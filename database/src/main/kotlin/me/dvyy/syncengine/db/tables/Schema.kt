package me.dvyy.syncengine.db.tables

object JsonTable : Table(
    """
    CREATE TABLE IF NOT EXISTS jsondata (
        id BLOB PRIMARY KEY,
        data BLOB
    )
    """.trimIndent()
)

object TasksView : View(
    """
    SELECT
        cast(data ->> '$.name' as TEXT) AS name,
        cast(data ->> '$.age' as INTEGER) as age
    from jsondata
    """.trimIndent(),
    setOf(JsonTable)
)

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
