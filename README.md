# syncengine

A cross-platform sync engine built on [sqlite-kt](https://github.com/MineInAbyss/sqlite-kt) made for my [task app](https://github.com/0ffz/tasks).
I currently don't recommend using this library directly, only as a reference for your own projects!


## Architecture

We store an action queue inside a local sqlite database. Local changes are applied immediately, then synced by sending local actions via websocket to a server (also running a sqlite database).

The server sends back exact row updates and client does a rollback and re-apply to reconcile changes. Rollbacks are handled by having two data columns and copying between them when local updates are made.
