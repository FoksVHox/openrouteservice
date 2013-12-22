package com.graphhopper.storage;

/**
 * You need custom storages, like turn cost tables, or osmid tables for your
 * graph? Implement this interface now and put it in any graph storage you want.
 * It's that easy!
 */
public interface ExtendedStorage
{

    /**
     * @return true, if and only if, if an additional field at the graphs node
     *         storage is required
     */
    boolean isRequireNodeField();

    /**
     * @return true, if and only if, if an additional field at the graphs edge
     *         storage is required
     */
    boolean isRequireEdgeField();
    
    /**
     * @return the default field value which will be set for default when creating nodes 
     */
    int getDefaultNodeFieldValue();
    
    /**
     * @return the default field value which will be set for default when creating edges 
     */
    int getDefaultEdgeFieldValue();

    /**
     * initializes the extended storage by giving the graph storage
     */
    void init( GraphStorage graph );

    /**
     * creates all additional data storages
     */
    void create( long initSize );

    /**
     * loads from existing data storages
     */
    boolean loadExisting();

    /**
     * sets the segment size in all additional data storages
     */
    void setSegmentSize( int bytes );

    /**
     * flushes all additional data storages
     */
    void flush();

    /**
     * closes all additional data storages
     */
    void close();

    /**
     * returns the sum of all additional data storages capacity
     */
    long getCapacity();

    /**
     * creates a copy of this extended storage
     */
    ExtendedStorage copyTo( ExtendedStorage extStorage );

    /**
     * default implementation defines no additional fields or any logic. there's like nothing
     * , like the default behavior.
     */
    public class NoExtendedStorage implements ExtendedStorage
    {

        @Override
        public boolean isRequireNodeField()
        {
            return false;
        }

        @Override
        public boolean isRequireEdgeField()
        {
            return false;
        }

        @Override
        public int getDefaultNodeFieldValue()
        {
            return 0;
        }

        @Override
        public int getDefaultEdgeFieldValue()
        {
            return 0;
        }
        
        @Override
        public void init( GraphStorage grap )
        {
            // noop
        }

        @Override
        public void create( long initSize )
        {
            // noop
        }

        @Override
        public boolean loadExisting()
        {
            // noop
            return true;
        }

        @Override
        public void setSegmentSize( int bytes )
        {
            // noop
        }

        @Override
        public void flush()
        {
            // noop
        }

        @Override
        public void close()
        {
            // noop
        }

        @Override
        public long getCapacity()
        {
            return 0;
        }

        @Override
        public ExtendedStorage copyTo( ExtendedStorage extStorage )
        {
            // noop
            return extStorage;
        }

    }
}
