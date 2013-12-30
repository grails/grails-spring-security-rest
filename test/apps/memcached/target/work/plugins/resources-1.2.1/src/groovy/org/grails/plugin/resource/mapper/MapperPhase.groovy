package org.grails.plugin.resource.mapper

/**
 * The ORDERED phases of mapper execution.
 * Change the order, you break everything. 
 */
enum MapperPhase {
    GENERATION, // create new assets = compile less files
    MUTATION, // alter/improve assets (may mean creating new/deleting aggregated resources) = spriting
    COMPRESSION, // reducing the file size but maintaining semantics = minify
    LINKNORMALISATION, // convert all inter asset references into a normal form = css links
    AGGREGATION, // combining mutiple assets into one = bundling
    RENAMING, // moving of physical assets = hashing
    LINKREALISATION, // convert normalised inter asset references into real form = css links
    ALTERNATEREPRESENTATION, // attach different representations of the asset = gzipping
    DISTRIBUTION, // moving assets to their hosting environment = s3, cdn
    ABSOLUTISATION, // update inter asset references to their distributed equivalent
    NOTIFICATION // let the world know about the new resources = cache invalidation
}

