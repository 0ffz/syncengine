package me.dvyy.syncengine.db.tables


object SubtaskRelation : Table(
    """
    CREATE TABLE IF NOT EXISTS subtask (
        parent BLOB,
        child BLOB,
        rank TEXT,
        PRIMARY KEY (parent, child)
    );
    CREATE INDEX subtask_relation ON subtask(parent);
    """.trimIndent()
)

object TaskTags : Table(
    """
    CREATE TABLE IF NOT EXISTS task_tags (
        id BLOB PRIMARY KEY,
        tag TEXT,
        PRIMARY KEY (id, tag),
        FOREIGN KEY (id) REFERENCES jsondata(id) ON DELETE CASCADE
    );
    CREATE INDEX idx_task_tags_tag ON task_tags(tag);
""".trimIndent()
)
