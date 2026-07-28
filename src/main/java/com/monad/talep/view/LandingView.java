package com.monad.talep.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Giris oncesi acilis sayfasi: navbar + glow'lu hero + dashboard mockup + bento ozellikler + SSS (ikonsuz, sade) */
@Route("")
@AnonymousAllowed
public class LandingView extends VerticalLayout {

    public LandingView() {
        setPadding(false);
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background", "var(--lumo-base-color)");
        setWidthFull();

        Div page = new Div();
        page.getStyle().set("width", "100%").set("background", "#0A0B14").set("overflow", "hidden");
        page.add(navbar(), hero());
        add(page);

        H2 featTitle = new H2("Neler Yapabilirsiniz?");
        featTitle.getStyle().set("margin-top", "3rem").set("text-align", "center");
        add(featTitle);
        add(featureGrid());

        add(faqSection());

        Paragraph footer = new Paragraph("TT Talep Destek Sistemi © 2026 — Monad Yazılım Staj Projesi");
        footer.getStyle().set("color", "#94a3b8").set("padding-bottom", "1.5rem");
        add(footer);
    }

    private Div navbar() {
        Div nav = new Div();
        nav.getStyle()
                .set("display", "flex").set("align-items", "center").set("justify-content", "space-between")
                .set("padding", "1.2rem 4vw").set("position", "relative").set("z-index", "5")
                .set("box-sizing", "border-box").set("width", "100%");

        HorizontalLayout brand = new HorizontalLayout();
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        VerticalLayout brandText = new VerticalLayout();
        brandText.setPadding(false);
        brandText.setSpacing(false);
        Span brandName = new Span("TT Talep");
        brandName.getStyle().set("font-family", "'Space Grotesk', sans-serif").set("font-weight", "700")
                .set("color", "white").set("font-size", "1.02rem").set("display", "block");
        Span brandSub = new Span("Destek Sistemi");
        brandSub.getStyle().set("color", "#8C97B5").set("font-size", "0.68rem").set("display", "block");
        brandText.add(brandName, brandSub);
        brand.add(brandText);

        HorizontalLayout links = new HorizontalLayout();
        links.getStyle().set("gap", "2rem").set("display", "flex");
        for (String l : new String[]{"Ana Sayfa", "Özellikler", "Nasıl Çalışır?", "Hakkında", "İletişim"}) {
            Span s = new Span(l);
            s.getStyle().set("color", "#C7CEDD").set("font-size", "0.92rem");
            links.add(s);
        }

        Button loginBtn = new Button("Giriş Yap →", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginBtn.getStyle().set("color", "white").set("font-weight", "500");
        Button signupBtn = new Button("Kayıt Ol", e -> getUI().ifPresent(ui -> ui.navigate("register")));
        signupBtn.getStyle().set("background", "linear-gradient(135deg, #8B5CF6, #EC4899)")
                .set("color", "white").set("border", "none").set("border-radius", "999px")
                .set("font-weight", "600").set("padding", "0 1.3rem");
        HorizontalLayout ctas = new HorizontalLayout(loginBtn, signupBtn);
        ctas.setAlignItems(FlexComponent.Alignment.CENTER);

        nav.add(brand, links, ctas);
        return nav;
    }

    private Div hero() {
        Div hero = new Div();
        hero.getStyle()
                .set("position", "relative").set("padding", "3.5rem 4vw 5rem")
                .set("display", "flex").set("align-items", "center").set("gap", "3rem")
                .set("flex-wrap", "wrap").set("box-sizing", "border-box").set("width", "100%");

        Div glow = new Div();
        glow.getStyle()
                .set("position", "absolute").set("top", "-200px").set("right", "-100px")
                .set("width", "700px").set("height", "700px")
                .set("background", "radial-gradient(circle, rgba(139,92,246,0.35) 0%, rgba(236,72,153,0.15) 40%, transparent 70%)")
                .set("filter", "blur(40px)").set("z-index", "0");
        hero.add(glow);

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(false);
        left.getStyle().set("flex", "1 1 420px").set("max-width", "560px").set("position", "relative").set("z-index", "2");

        Span badge = new Span("✦ Akıllı önceliklendirme ile fark yaratın");
        badge.getStyle().set("background", "rgba(139,92,246,0.18)").set("color", "#C4B5FD")
                .set("font-size", "0.78rem").set("padding", "0.35rem 0.9rem").set("border-radius", "999px")
                .set("display", "inline-block").set("margin-bottom", "1.2rem").set("width", "fit-content");

        Html title = new Html("<div style='font-family:\"Space Grotesk\",sans-serif;font-size:2.9rem;"
                + "line-height:1.15;font-weight:700;color:white;'>"
                + "<span style='display:block;'>Taleplerinizi yönetin,</span>"
                + "<span style='background:linear-gradient(135deg,#EC4899,#F97316);"
                + "-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent;"
                + "color:transparent;'>değer</span>"
                + "<span style='color:white;'> yaratın.</span></div>");

        Paragraph desc = new Paragraph("Müşteri taleplerini toplayın, önceliklendirin, göreve dönüştürün ve tek merkezden takip edin.");
        desc.getStyle().set("color", "#C7CEDD").set("font-size", "1.05rem").set("margin-top", "1.1rem")
                .set("max-width", "460px").set("line-height", "1.6");

        Button loginBtn = new Button("Giriş Yap →", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_LARGE);
        loginBtn.getStyle().set("background", "linear-gradient(135deg, #8B5CF6, #EC4899)")
                .set("color", "white").set("border-radius", "999px").set("font-weight", "600").set("border", "none");
        Button registerBtn = new Button("Hesap Oluştur →", e -> getUI().ifPresent(ui -> ui.navigate("register")));
        registerBtn.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_TERTIARY);
        registerBtn.getStyle().set("color", "white").set("border", "1px solid rgba(255,255,255,0.25)")
                .set("border-radius", "999px").set("font-weight", "600");
        HorizontalLayout ctas = new HorizontalLayout(loginBtn, registerBtn);
        ctas.getStyle().set("margin-top", "1.8rem").set("flex-wrap", "wrap");

        HorizontalLayout tags = new HorizontalLayout(
                tag("#8B5CF6", "Akıllı Önceliklendirme"),
                tag("#3B82F6", "Görev Yönetimi"),
                tag("#10B981", "Canlı Takip"),
                tag("#F97316", "Raporlama"));
        tags.getStyle().set("margin-top", "2.2rem").set("flex-wrap", "wrap").set("gap", "1.5rem");

        left.add(badge, title, desc, ctas, tags);

        hero.add(left, mockPreview());
        return hero;
    }

    private HorizontalLayout tag(String color, String text) {
        Div dot = new Div();
        dot.getStyle().set("width", "7px").set("height", "7px").set("border-radius", "50%").set("background", color);
        Span s = new Span(text);
        s.getStyle().set("color", "#C7CEDD").set("font-size", "0.82rem");
        HorizontalLayout h = new HorizontalLayout(dot, s);
        h.setAlignItems(FlexComponent.Alignment.CENTER);
        h.getStyle().set("gap", "0.4rem");
        return h;
    }

    private Div mockPreview() {
        Div frame = new Div();
        frame.getStyle()
                .set("flex", "1 1 480px").set("max-width", "560px").set("position", "relative").set("z-index", "2")
                .set("background", "#12121F").set("border-radius", "18px")
                .set("box-shadow", "0 30px 80px rgba(0,0,0,0.5)")
                .set("border", "1px solid rgba(255,255,255,0.08)").set("overflow", "hidden");

        Div topbar = new Div();
        topbar.getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "space-between")
                .set("padding", "0.8rem 1.2rem").set("background", "rgba(255,255,255,0.03)");
        Span mText = new Span("TT Talep");
        mText.getStyle().set("color", "white").set("font-size", "0.85rem").set("font-weight", "600");
        Div avatar = new Div();
        avatar.setText("TT");
        avatar.getStyle().set("background", "#8B5CF6").set("color", "white").set("border-radius", "50%")
                .set("width", "22px").set("height", "22px").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center").set("font-size", "0.6rem");
        topbar.add(mText, avatar);

        Div body = new Div();
        body.getStyle().set("display", "flex");

        Div sidebar = new Div();
        sidebar.getStyle().set("width", "120px").set("background", "rgba(255,255,255,0.02)")
                .set("padding", "1rem 0.6rem").set("font-size", "0.75rem").set("flex", "0 0 auto");
        String[] sideItems = {"Dashboard", "Talepler", "Görevler", "Müşteriler", "Raporlar", "Ayarlar"};
        for (int i = 0; i < sideItems.length; i++) {
            Div item = new Div();
            item.setText(sideItems[i]);
            item.getStyle().set("padding", "0.5rem 0.6rem").set("border-radius", "8px")
                    .set("color", i == 0 ? "#C4B5FD" : "#8C97B5")
                    .set("background", i == 0 ? "rgba(139,92,246,0.2)" : "transparent")
                    .set("margin-bottom", "0.3rem");
            sidebar.add(item);
        }

        Div content = new Div();
        content.getStyle().set("flex", "1").set("padding", "1rem 1.2rem").set("min-width", "0");

        Div statGrid = new Div();
        statGrid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(2, 1fr)")
                .set("gap", "0.7rem").set("margin-bottom", "1rem");
        statGrid.add(
                statCard("Toplam Talep", "1.248", "↑ %12 bu ay", "#34D399"),
                statCard("Bekleyen Talep", "342", "↓ %8 bu ay", "#FBBF24"),
                statCard("Tamamlanan", "906", "↑ %15 bu ay", "#34D399"),
                statCard("Ort. Çözüm Süresi", "2.4 gün", "↓ %5 bu ay", "#34D399"));

        Div chartRow = new Div();
        chartRow.getStyle().set("display", "flex").set("gap", "0.7rem");

        Div lineCard = new Div();
        lineCard.getStyle().set("flex", "1.4").set("background", "rgba(255,255,255,0.04)")
                .set("border-radius", "10px").set("padding", "0.8rem").set("min-width", "0");
        Span lineTitle = new Span("Talep Dağılımı (Son 7 Ay)");
        lineTitle.getStyle().set("display", "block").set("font-size", "0.72rem").set("color", "#8C97B5")
                .set("margin-bottom", "0.5rem");
        Html lineSvg = new Html("<svg xmlns='http://www.w3.org/2000/svg' width='100%' height='70' viewBox='0 0 200 70'>"
                + "<polyline fill='none' stroke='#10B981' stroke-width='2' "
                + "points='0,50 30,35 60,45 90,20 120,30 150,10 180,25' /></svg>");
        lineCard.add(lineTitle, lineSvg);

        Div donutCard = new Div();
        donutCard.getStyle().set("flex", "1").set("background", "rgba(255,255,255,0.04)")
                .set("border-radius", "10px").set("padding", "0.8rem").set("min-width", "0");
        Span donutTitle = new Span("Talep Durumu");
        donutTitle.getStyle().set("display", "block").set("font-size", "0.72rem").set("color", "#8C97B5")
                .set("margin-bottom", "0.5rem");
        Div donut = new Div();
        donut.getStyle().set("width", "60px").set("height", "60px").set("border-radius", "50%")
                .set("margin", "0 auto 0.6rem")
                .set("background", "conic-gradient(#8B5CF6 0deg 90deg, #3B82F6 90deg 216deg, #F97316 216deg 288deg, #10B981 288deg 360deg)");
        donutCard.add(donutTitle, donut,
                legendLine("#8B5CF6", "Yeni %25"), legendLine("#3B82F6", "İncelemede %35"),
                legendLine("#F97316", "Devam Ediyor %20"), legendLine("#10B981", "Tamamlandı %20"));

        chartRow.add(lineCard, donutCard);
        content.add(statGrid, chartRow);
        body.add(sidebar, content);

        frame.add(topbar, body);
        return frame;
    }

    private Div statCard(String label, String value, String delta, String deltaColor) {
        Div card = new Div();
        card.getStyle().set("background", "rgba(255,255,255,0.04)").set("border-radius", "10px")
                .set("padding", "0.7rem 0.9rem");
        Span l = new Span(label);
        l.getStyle().set("display", "block").set("font-size", "0.68rem").set("color", "#8C97B5");
        Span v = new Span(value);
        v.getStyle().set("display", "block").set("font-size", "1.3rem").set("font-weight", "700")
                .set("color", "white").set("margin-top", "0.2rem")
                .set("font-family", "'Space Grotesk', sans-serif");
        Span d = new Span(delta);
        d.getStyle().set("display", "block").set("font-size", "0.65rem").set("margin-top", "0.2rem").set("color", deltaColor);
        card.add(l, v, d);
        return card;
    }

    private Span legendLine(String color, String text) {
        Span s = new Span("● " + text);
        s.getStyle().set("display", "block").set("font-size", "0.65rem").set("color", color).set("margin-bottom", "0.2rem");
        return s;
    }

    /** Alt cizgili, sade gradient-serit 4'lu ozellik karti izgarasi (ikonsuz). */
    private Div featureGrid() {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid").set("grid-template-columns", "repeat(4, 1fr)")
                .set("gap", "1.2rem").set("width", "min(1100px, 92vw)").set("margin", "0 0 3rem");

        grid.add(featureCard("#8B5CF6", "Talep Oluşturun",
                "Müşteriler saniyeler içinde talep oluşturabilir ve sisteme iletebilir."));
        grid.add(featureCard("#3B82F6", "Akıllı Önceliklendirme",
                "Etki, aciliyet ve AHP + TOPSIS yöntemiyle talepleri doğru şekilde önceliklendirin."));
        grid.add(featureCard("#F97316", "Görev Takibi",
                "Onaylanan talepleri görevlere dönüştürün ve süreci adım adım takip edin."));
        grid.add(featureCard("#10B981", "Canlı Dashboard",
                "İstatistikleri anlık görüntüleyin, ekibinizin performansını takip edin."));
        return grid;
    }

    private Div featureCard(String accent, String title, String desc) {
        Div card = new Div();
        card.getStyle()
                .set("background", "#12121F").set("border-radius", "16px").set("overflow", "hidden")
                .set("border-bottom", "3px solid " + accent);

        Div visual = new Div();
        visual.getStyle()
                .set("height", "80px")
                .set("background", "linear-gradient(135deg, " + accent + "55 0%, rgba(255,255,255,0.02) 100%)");

        Div text = new Div();
        text.getStyle().set("padding", "1.1rem 1.2rem 1.4rem");
        H4 t = new H4(title);
        t.getStyle().set("color", "white").set("font-size", "1rem").set("margin", "0 0 0.4rem 0");
        Span d = new Span(desc);
        d.getStyle().set("color", "#9DA4B8").set("font-size", "0.82rem").set("line-height", "1.5");
        text.add(t, d);

        card.add(visual, text);
        return card;
    }

    private VerticalLayout faqSection() {
        H2 faqTitle = new H2("Sıkça Sorulan Sorular");
        add(faqTitle);

        Accordion faq = new Accordion();
        faq.add("Nasıl talep oluşturabilirim?",
                p("Müşteri Kaydı Oluştur butonuyla ücretsiz hesap açın, giriş yaptıktan sonra 'Taleplerim' ekranından başlık ve açıklama girerek talebinizi gönderin."));
        faq.add("Talebimin durumunu nasıl takip ederim?",
                p("Taleplerim ekranında her talebinizin güncel durumu görünür: Yeni → İncelemede → Önceliklendirildi → Göreve Dönüştü. Her durum değişiminde bildirim alırsınız."));
        faq.add("Öncelik skoru nasıl hesaplanıyor?",
                p("Ürün Sorumlusu talebinizi Etki ve Aciliyet (1-5) kriterlerine göre değerlendirir. Basit modda skor = Etki × Aciliyet; AHP modunda Saaty ölçeğiyle ağırlıklı hesaplama, TOPSIS modunda ise tüm bekleyen talepler birlikte kıyaslanarak sıralanır."));
        faq.add("Şifremi unuttum, ne yapmalıyım?",
                p("Giriş ekranındaki 'Şifremi Unuttum' bağlantısına tıklayın. E-posta adresinize geçici şifre gönderilir."));
        faq.add("Bir talepten birden fazla görev açılabilir mi?",
                p("Evet. Büyük talepler ürün sorumlusu tarafından birden fazla göreve bölünerek farklı yazılımcılara atanabilir."));
        faq.add("Kimler sisteme erişebilir?",
                p("Dört rol vardır: Müşteri (talep açar), Ürün Sorumlusu (önceliklendirir), Yazılımcı (görevleri yürütür) ve Admin (kullanıcıları yönetir). Her rol yalnızca kendi ekranlarını görür."));
        faq.setWidth("min(720px, 92vw)");
        faq.getStyle().set("margin-bottom", "2rem").set("margin-top", "1.5rem");

        VerticalLayout wrap = new VerticalLayout(faq);
        wrap.setPadding(false);
        wrap.setAlignItems(FlexComponent.Alignment.CENTER);
        return wrap;
    }

    private Paragraph p(String text) {
        return new Paragraph(text);
    }
}