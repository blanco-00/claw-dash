-- Add source_handle and target_handle columns to store handle positions for edges
ALTER TABLE config_graph_edges 
ADD COLUMN source_handle VARCHAR(50) DEFAULT NULL,
ADD COLUMN target_handle VARCHAR(50) DEFAULT NULL;
