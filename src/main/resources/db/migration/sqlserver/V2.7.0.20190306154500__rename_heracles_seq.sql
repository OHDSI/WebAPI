ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA DROP CONSTRAINT df_heracles_vis_data_id;
GO

exec sp_rename @objname = '${ohdsiSchema}.heracles_visualization_data_sequence', @newname = 'heracles_vis_data_sequence';

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA 
   ADD CONSTRAINT df_heracles_vis_data_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.heracles_vis_data_sequence) FOR [id];