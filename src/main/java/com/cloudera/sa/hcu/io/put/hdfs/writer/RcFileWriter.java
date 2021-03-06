package com.cloudera.sa.hcu.io.put.hdfs.writer;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.RCFile;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;

import com.cloudera.sa.hcu.utils.PropertyUtils;

public class RcFileWriter extends AbstractWriter
{
	RCFile.Writer writer;
	int maxColumns;	
	
	public static final String CONF_MAX_COLUMNS = "writer.max.columns";
	public static final String CONF_COMPRESSION_CODEC = COMPRESSION_CODEC;
	
	public RcFileWriter(Properties p) throws Exception
	{
		super(p);
	}
	
	public RcFileWriter(String outputPath, int maxColumns, String compressionCodec) throws IOException
	{
		super(makeProperties(outputPath, maxColumns, compressionCodec));
	}
	
	private static Properties makeProperties(String outputPath, int maxColumns, String compressionCodec)
	{
		Properties p = new Properties();
		
		p.setProperty(CONF_OUTPUT_PATH, outputPath);
		p.setProperty(CONF_MAX_COLUMNS, Integer.toString(maxColumns));
		p.setProperty(CONF_COMPRESSION_CODEC, compressionCodec);
		
		return p;
	}
	
	@Override
	protected void init(String outputPath, Properties p) throws IOException
	{	
		this.maxColumns = PropertyUtils.getIntProperty(p, CONF_MAX_COLUMNS);
		
		//Open hdfs file system
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(config);
		
		//Create path object
		Path outputFilePath = new Path(outputPath);
		
		config.set(RCFile.COLUMN_NUMBER_CONF_STR, Integer.toString(maxColumns));
		writer = new RCFile.Writer(hdfs, config, outputFilePath, null, PropertyUtils.getCompressionCodecProperty(p, CONF_COMPRESSION_CODEC ));
	}

	public void writeRow(String rowType, String[] columns) throws IOException
	{
		BytesRefArrayWritable arrayVal = new BytesRefArrayWritable(maxColumns);
			
		int columnIndex = 0;
		for (String columnValue: columns)
		{
			BytesRefWritable byteValue = new BytesRefWritable();
			byteValue.set(columnValue.getBytes(), 0, columnValue.length());
				
			arrayVal.set(columnIndex, byteValue);
					
			columnIndex++;;
		}
			
		writer.append( arrayVal );
	}

	public void close() throws IOException
	{
		writer.close();
		
	}

	
}
