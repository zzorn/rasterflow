Rasterflow
==========

A library for raster image manipulation.

Planned features:
* Tile based raster - allowing arbitrary sized pictures to be created
* Float channels - for high dynamic range, and for generating heightmaps
* Nested layers - with some operations to combine layers into a final multi-channel raster
* Render operation - to draw some arbitrary content on the channels of a layer
* Draw image operation - to draw (some of) the contents of another raster to another with an arbitrary transformation
* Generators - to generate layer contents using some procedural means
* View with zoom, rotation, pan - render the visible and generated layers to the specified view area.
* Progressive update of the view - render fast sketch first, update view as rendering proceeds
* Multi-threading support for generators, rendering, copying - to take advantage of multi-core CPU:s.
* Full undo-redo queue - stores operations, caches or re-generates image tiles
* Rendering masks, protecting layer contents from change.
