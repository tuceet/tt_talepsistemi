package com.monad.talep.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.monad.talep.entity.Request;

/**
 * TOPSIS (Technique for Order Preference by Similarity to Ideal Solution).
 *
 * AHP tek bir talebin skorunu (agirlikli toplam) hesaplar; TOPSIS ise BIRDEN FAZLA
 * talebi ayni anda, ayni kriter uzayinda kiyaslayip ideal cozume yakinliklarina
 * gore siralar. Burada AHP'den gelen agirliklar (wUrgency, wImpact) TOPSIS'in
 * girdisi olarak kullanilir -> iki yontem birbirini tamamlar (AHP+TOPSIS).
 *
 * Adimlar:
 *  1) Vektor normalizasyonu: r_ij = x_ij / sqrt(sum(x_kj^2))
 *  2) Agirlikli normalize matris: v_ij = r_ij * w_j
 *  3) Ideal en iyi (A+) ve ideal en kotu (A-) (iki kriter de fayda tipi -> max/min)
 *  4) Her alternatif icin A+ ve A-'ya oklid uzakligi
 *  5) Yakinlik katsayisi: C_i = d(A-) / (d(A+) + d(A-))  [0..1, buyuk = daha oncelikli]
 */
@Service
public class TopsisService {

    public record Alternative(Request request, int urgency, int impact) {}

    public record TopsisResult(Request request, int urgency, int impact, double closeness) {}

    public List<TopsisResult> rank(List<Alternative> alternatives, double wUrgency, double wImpact) {
        int n = alternatives.size();
        if (n == 0) return List.of();

        double sumSqUrgency = 0, sumSqImpact = 0;
        for (Alternative a : alternatives) {
            sumSqUrgency += (double) a.urgency() * a.urgency();
            sumSqImpact += (double) a.impact() * a.impact();
        }
        double normUrgency = Math.sqrt(sumSqUrgency);
        double normImpact = Math.sqrt(sumSqImpact);

        double[] vUrgency = new double[n];
        double[] vImpact = new double[n];
        for (int i = 0; i < n; i++) {
            Alternative a = alternatives.get(i);
            vUrgency[i] = (normUrgency == 0 ? 0 : a.urgency() / normUrgency) * wUrgency;
            vImpact[i] = (normImpact == 0 ? 0 : a.impact() / normImpact) * wImpact;
        }

        double bestUrgency = Arrays.stream(vUrgency).max().orElse(0);
        double bestImpact = Arrays.stream(vImpact).max().orElse(0);
        double worstUrgency = Arrays.stream(vUrgency).min().orElse(0);
        double worstImpact = Arrays.stream(vImpact).min().orElse(0);

        List<TopsisResult> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double dBest = Math.sqrt(Math.pow(vUrgency[i] - bestUrgency, 2) + Math.pow(vImpact[i] - bestImpact, 2));
            double dWorst = Math.sqrt(Math.pow(vUrgency[i] - worstUrgency, 2) + Math.pow(vImpact[i] - worstImpact, 2));
            double closeness = (dBest + dWorst) == 0 ? 0 : dWorst / (dBest + dWorst);
            Alternative a = alternatives.get(i);
            results.add(new TopsisResult(a.request(), a.urgency(), a.impact(), round(closeness)));
        }
        results.sort((x, y) -> Double.compare(y.closeness(), x.closeness()));
        return results;
    }

    private double round(double v) { return Math.round(v * 10000.0) / 10000.0; }
}