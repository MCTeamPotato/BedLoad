# BedLoad
This is a simple server-side mod that allows you to define any blocks as ChunkLoader.

Default chunk loader: beds.

## Details
- When a chunk loader block takes effect, all players will be notified. (configurable)
- chunk loaders don't have radius. It will only force-load its current chunk.
- Config reloading supported.
## Config

```toml
[BedLoad]
	ChunkLoaderBlocks = []
	ChunkLoaderBlockTags = ["minecraft:beds"]
	NotePlayersOnChunkLoaderUpdate = true

```
## Note
If you find a chunk loader block cannot work well, try to re-place it.