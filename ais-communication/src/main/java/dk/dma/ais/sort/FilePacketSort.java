/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.sort;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.reader.AisStreamReader;
import dk.dma.enav.util.function.Consumer;

/**
 * Class to sort an arbitrary large file of NMEA messages 
 */
public class FilePacketSort {
    
    private static final Logger LOG = LoggerFactory.getLogger(FilePacketSort.class);
    
    private final String inFile;
    private final String outFile;
    private final Comparator<AisPacket> comparator;
    private final PacketSort sorter;
    private int bucketSize = 100000;
    private String workDirectory;
    
    private int bucketCount = 1;
    private final ArrayList<String> bucketFiles = new ArrayList<>();
    
    public FilePacketSort(String inFile, String outFile) {
        this(inFile, outFile, null);
    }
    
    public FilePacketSort(String inFile, String outFile, Comparator<AisPacket> comparator) {
        this.inFile = inFile;
        this.outFile = outFile;
        this.comparator = comparator;
        // Set default work directory to CWD
        workDirectory = ".";
        sorter = new PacketSort(comparator);
    }        
    
    /**
     * Set the working directory where temporary files are stored 
     */
    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }
    
    /**
     * Set the number of message to be allowed to be sorted in memory
     * @param bucketSize
     */
    public void setBucketSize(int bucketSize) {
        this.bucketSize = bucketSize;
    }
    
    public void setTmpDirectory(String tmpDirectory) {
        this.workDirectory = tmpDirectory;
    }
    
    public void sort() throws InterruptedException, IOException {
        // Read into buckets, sort and save to temporary files
        final List<AisPacket> bucket = new ArrayList<>();
        InputStream in = new FileInputStream(inFile);
        AisStreamReader reader = new AisStreamReader(in);
        reader.registerPacketHandler(new Consumer<AisPacket>() {
            @Override
            public void accept(AisPacket packet) {
                bucket.add(packet);
                if (bucket.size() == bucketSize) {
                    // Save bucket to file and clear
                    try {
                        saveBucket(bucket);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        // HOW to handle?
                    }
                    bucket.clear();
                    // Increment bucket count
                    bucketCount++;
                }
                
                
            }
        });
        reader.start();
        reader.join();
        in.close();        
        
        // Maybe save last bucket if not empty
        if (bucket.size() > 0) {
            saveBucket(bucket);
        }        
        
        LOG.info("Read packets into " + bucketCount + " files");
        
        
    }
    
    private void saveBucket(List<AisPacket> bucket) throws IOException {
        // Sort bucket
        sorter.sort(bucket);
        
        // Make filename based on bucketCount
        String filename = makeFilename();
        
        // Save to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(makePath(filename)));
        for (AisPacket packet : bucket) {
            writer.write(packet.getStringMessage() + "\r\n");
        }
        writer.close();
        
        // Add filename to list of buckets        
        
    }
    
    private String makeFilename() {
        return String.format("%s_tmp_%05d", outFile, bucketCount);
    }
    
    private String makePath(String filename) {
        return workDirectory + "/" + filename;
    }

}
