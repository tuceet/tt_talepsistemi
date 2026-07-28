-- =====================================================
-- MOCK / TEST VERILERI - ORACLE
-- DDL'den SONRA calistir. En sonda COMMIT var, silme!
-- =====================================================
INSERT INTO tt_roles (role_name) VALUES ('CUSTOMER');
INSERT INTO tt_roles (role_name) VALUES ('PRODUCT_OWNER');
INSERT INTO tt_roles (role_name) VALUES ('DEVELOPER');
INSERT INTO tt_roles (role_name) VALUES ('ADMIN');

INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Ahmet Yilmaz','ahmet@musteri.com','$2a$10$dummyhash1',1);
INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Zeynep Kaya','zeynep@musteri.com','$2a$10$dummyhash2',1);
INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Mert Demir','mert@monad.com','$2a$10$dummyhash3',2);
INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Elif Celik','elif@monad.com','$2a$10$dummyhash4',3);
INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Can Arslan','can@monad.com','$2a$10$dummyhash5',3);
INSERT INTO tt_users (name_surname, email, password_hash, role_id) VALUES ('Tugce Toprak','tugce@monad.com','$2a$10$dummyhash6',4);

INSERT INTO tt_projects (project_name, description) VALUES ('CRM Modernizasyon','Musteri iliskileri sistemi yenileme');
INSERT INTO tt_projects (project_name, description) VALUES ('Mobil Uygulama','iOS/Android musteri uygulamasi');

INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (1,1,'Login ekrani hata veriyor','Kullanici girisinde 500 hatasi aliniyor, acil cozum gerekli.','NEW');
INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (1,1,'Rapor ekrani cok yavas','Aylik satis raporu 30 saniyede aciliyor.','UNDER_REVIEW');
INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (2,2,'Push bildirim ozelligi','Mobil uygulamaya kampanya bildirimi eklensin.','PRIORITIZED');
INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (2,2,'Karanlik mod istegi','Uygulamaya dark mode secenegi eklensin.','PRIORITIZED');
INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (1,1,'Excel export butonu','Talep listesi Excel olarak indirilebilsin.','CONVERTED');
INSERT INTO tt_requests (customer_id, project_id, title, description, status) VALUES (2,1,'Logo degisikligi','Eski logo hala gorunuyor bazi sayfalarda.','REJECTED');

INSERT INTO tt_prioritizations (request_id, urgency, impact, priority_score, method, evaluated_by) VALUES (3,4,5,4.75,'AHP',3);
INSERT INTO tt_prioritizations (request_id, urgency, impact, priority_score, method, evaluated_by) VALUES (4,2,3,6,'SIMPLE',3);
INSERT INTO tt_prioritizations (request_id, urgency, impact, priority_score, method, evaluated_by) VALUES (5,5,4,20,'SIMPLE',3);

INSERT INTO tt_ahp_evaluations (request_id, comparison_value, weight_impact, weight_urgency, consistency_ratio, final_score, evaluated_by)
VALUES (3, 3, 0.75, 0.25, 0.0, 4.75, 3);

INSERT INTO tt_workflows (request_id, developer_id, task_title, workflow_status) VALUES (5,4,'Backend export servisi','IN_PROGRESS');
INSERT INTO tt_workflows (request_id, developer_id, task_title, workflow_status) VALUES (5,5,'UI export butonu','BACKLOG');
INSERT INTO tt_workflows (request_id, developer_id, task_title, workflow_status) VALUES (3,4,'Push bildirim entegrasyonu','ASSIGNED');

INSERT INTO tt_request_status_history (request_id, old_status, new_status, changed_by) VALUES (5,'NEW','UNDER_REVIEW',3);
INSERT INTO tt_request_status_history (request_id, old_status, new_status, changed_by) VALUES (5,'UNDER_REVIEW','PRIORITIZED',3);
INSERT INTO tt_request_status_history (request_id, old_status, new_status, changed_by) VALUES (5,'PRIORITIZED','CONVERTED',3);

INSERT INTO tt_comments (request_id, user_id, content) VALUES (1,3,'Log kayitlarini inceliyoruz, hafta ici donus yapilacak.');
INSERT INTO tt_comments (request_id, user_id, content) VALUES (5,4,'Export servisi yarin test ortaminda.');
INSERT INTO tt_notifications (user_id, message) VALUES (1,'Talebiniz (#1) incelemeye alindi.');
INSERT INTO tt_notifications (user_id, message) VALUES (4,'Size yeni gorev atandi: Backend export servisi');
INSERT INTO tt_activity_log (user_id, action_type, entity_type, entity_id, detail) VALUES (3,'PRIORITIZE','REQUEST',3,'AHP ile onceliklendirildi');
INSERT INTO tt_activity_log (user_id, action_type, entity_type, entity_id, detail) VALUES (3,'ASSIGN_TASK','TASK',1,'Elif Celik atandi');

COMMIT;
