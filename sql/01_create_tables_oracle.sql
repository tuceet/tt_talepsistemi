-- =====================================================
-- TALEP YONETIM SISTEMI - ORACLE DDL
-- PL/SQL Developer: File > New > SQL Window ac,
-- yapistir, tumunu sec, F8 (Execute) ile calistir.
-- =====================================================

CREATE TABLE tt_roles (
    role_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name   VARCHAR2(30) NOT NULL UNIQUE
);

CREATE TABLE tt_users (
    user_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name_surname  VARCHAR2(100) NOT NULL,
    email         VARCHAR2(100) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    role_id       NUMBER NOT NULL,
    is_active     NUMBER(1) DEFAULT 1 NOT NULL,
    plan_type     VARCHAR2(20) DEFAULT 'FREE' NOT NULL,
    created_at    TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_users_role FOREIGN KEY (role_id) REFERENCES tt_roles(role_id),
    CONSTRAINT tt_chk_users_plan CHECK (plan_type IN ('FREE','PRO','PRO_PLUS'))
);

CREATE TABLE tt_projects (
    project_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_name VARCHAR2(150) NOT NULL,
    description  VARCHAR2(500),
    created_at   TIMESTAMP DEFAULT SYSTIMESTAMP
);

CREATE TABLE tt_requests (
    request_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER NOT NULL,
    project_id  NUMBER,
    title       VARCHAR2(200) NOT NULL,
    description CLOB NOT NULL,
    status      VARCHAR2(30) DEFAULT 'NEW' NOT NULL,
    created_at  TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at  TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_req_customer FOREIGN KEY (customer_id) REFERENCES tt_users(user_id),
    CONSTRAINT tt_fk_req_project  FOREIGN KEY (project_id) REFERENCES tt_projects(project_id),
    CONSTRAINT tt_chk_req_status CHECK (status IN
        ('NEW','UNDER_REVIEW','PRIORITIZED','REJECTED','CONVERTED'))
);

CREATE TABLE tt_request_status_history (
    history_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id NUMBER NOT NULL,
    old_status VARCHAR2(30),
    new_status VARCHAR2(30) NOT NULL,
    changed_by NUMBER NOT NULL,
    changed_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_hist_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_hist_user    FOREIGN KEY (changed_by) REFERENCES tt_users(user_id)
);

CREATE TABLE tt_prioritizations (
    priority_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id     NUMBER NOT NULL UNIQUE,
    urgency        NUMBER NOT NULL,
    impact         NUMBER NOT NULL,
    priority_score NUMBER(8,4),
    method         VARCHAR2(10) DEFAULT 'SIMPLE' NOT NULL,
    evaluated_by   NUMBER NOT NULL,
    created_at     TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_pri_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_pri_user    FOREIGN KEY (evaluated_by) REFERENCES tt_users(user_id),
    CONSTRAINT tt_chk_urgency CHECK (urgency BETWEEN 1 AND 5),
    CONSTRAINT tt_chk_impact  CHECK (impact BETWEEN 1 AND 5),
    CONSTRAINT tt_chk_method  CHECK (method IN ('SIMPLE','AHP'))
);

CREATE TABLE tt_ahp_evaluations (
    ahp_id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id        NUMBER NOT NULL UNIQUE,
    comparison_value  NUMBER(5,2) NOT NULL,
    weight_impact     NUMBER(6,4),
    weight_urgency    NUMBER(6,4),
    consistency_ratio NUMBER(6,4),
    final_score       NUMBER(8,4),
    evaluated_by      NUMBER NOT NULL,
    created_at        TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_ahp_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_ahp_user    FOREIGN KEY (evaluated_by) REFERENCES tt_users(user_id),
    CONSTRAINT tt_chk_ahp_cmp CHECK (comparison_value BETWEEN 0.11 AND 9)
);

CREATE TABLE tt_workflows (
    task_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id      NUMBER NOT NULL,
    developer_id    NUMBER,
    task_title      VARCHAR2(200) NOT NULL,
    workflow_status VARCHAR2(30) DEFAULT 'BACKLOG' NOT NULL,
    created_at      TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at      TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_wf_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_wf_dev     FOREIGN KEY (developer_id) REFERENCES tt_users(user_id),
    CONSTRAINT tt_chk_wf_status CHECK (workflow_status IN
        ('BACKLOG','ASSIGNED','IN_PROGRESS','TESTING','DONE'))
);

CREATE TABLE tt_comments (
    comment_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id NUMBER NOT NULL,
    user_id    NUMBER NOT NULL,
    content    VARCHAR2(2000) NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_cmt_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_cmt_user    FOREIGN KEY (user_id) REFERENCES tt_users(user_id)
);

CREATE TABLE tt_files (
    file_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id  NUMBER NOT NULL,
    file_name   VARCHAR2(255) NOT NULL,
    file_path   VARCHAR2(500) NOT NULL,
    uploaded_by NUMBER NOT NULL,
    uploaded_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_file_request FOREIGN KEY (request_id) REFERENCES tt_requests(request_id),
    CONSTRAINT tt_fk_file_user    FOREIGN KEY (uploaded_by) REFERENCES tt_users(user_id)
);

CREATE TABLE tt_notifications (
    notification_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER NOT NULL,
    message         VARCHAR2(500) NOT NULL,
    is_read         NUMBER(1) DEFAULT 0 NOT NULL,
    created_at      TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_notif_user FOREIGN KEY (user_id) REFERENCES tt_users(user_id)
);

CREATE TABLE tt_activity_log (
    log_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     NUMBER NOT NULL,
    action_type VARCHAR2(50) NOT NULL,
    entity_type VARCHAR2(30),
    entity_id   NUMBER,
    detail      VARCHAR2(500),
    created_at  TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_log_user FOREIGN KEY (user_id) REFERENCES tt_users(user_id)
);
CREATE TABLE tt_team_messages (
    message_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sender_id   NUMBER NOT NULL,
    content     VARCHAR2(1000) NOT NULL,
    created_at  TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT tt_fk_teammsg_user FOREIGN KEY (sender_id) REFERENCES tt_users(user_id)
);
