CREATE TYPE question_type_enum AS ENUM ('IMPACT', 'PROBABILITY');
CREATE TYPE feedback_type_enum AS ENUM ('HELP', 'SUGGESTION');

CREATE TABLE admin_users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO admin_users VALUES
(1, 'admin@example.com', '$2a$10$vX0ZpXqYBURAAnCS//tKdeLhIgnha4Sg5yXkdhAmreC1Ssx0LmIjm', 'admin');

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE questions (
    id SERIAL PRIMARY KEY,
    question_text VARCHAR(255) NOT NULL UNIQUE,
    category_id BIGINT,
    CONSTRAINT fk_category FOREIGN KEY(category_id) REFERENCES categories(id)
);

CREATE TABLE question_options (
    id SERIAL PRIMARY KEY,
    option_level VARCHAR(255) NOT NULL,
    option_text VARCHAR(255) NOT NULL,
	option_type VARCHAR(255) NOT NULL,
    recommendation TEXT,
    question_id BIGINT NOT NULL,
    CONSTRAINT fk_question FOREIGN KEY(question_id) REFERENCES questions(id)
);

CREATE TABLE questionnaire (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE questionnaire_questions (
    questionnaire_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    CONSTRAINT fk_questionnaire FOREIGN KEY(questionnaire_id) REFERENCES questionnaire(id),
    CONSTRAINT fk_question_in_qq FOREIGN KEY(question_id) REFERENCES questions(id)
);

CREATE TABLE answers (
    id SERIAL PRIMARY KEY,
    chosen_level VARCHAR(255),
    created_at TIMESTAMP(6),
    email VARCHAR(255) NOT NULL,
    question_id BIGINT NOT NULL,
    question_option_id BIGINT,
    question_text VARCHAR(255) NOT NULL,
    question_type question_type_enum,
    submission_id VARCHAR(255) NOT NULL,
    user_response VARCHAR(255) NOT NULL
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    email VARCHAR(255) NOT NULL,
    feedback_type feedback_type_enum NOT NULL,
    user_feedback VARCHAR(1000) NOT NULL
);

CREATE TABLE password_history (
    id SERIAL PRIMARY KEY,
    changed_at TIMESTAMP(6),
    password_hash VARCHAR(255),
    admin_user_id BIGINT,
    CONSTRAINT fk_admin_user FOREIGN KEY(admin_user_id) REFERENCES admin_users(id)
);
