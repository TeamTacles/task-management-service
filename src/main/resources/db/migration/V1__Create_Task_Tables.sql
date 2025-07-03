CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(60) NOT NULL,
    description VARCHAR(250),
    due_date TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL
);

CREATE TABLE task_responsibles (
    task_id BIGINT NOT NULL,
    responsible_user_id BIGINT,
    FOREIGN KEY (task_id) REFERENCES tasks(id)
);