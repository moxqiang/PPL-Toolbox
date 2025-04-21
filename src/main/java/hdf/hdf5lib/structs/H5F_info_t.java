/*****************************************************************************
 * Copyright by The HDF Group.                                               *
 * Copyright by the Board of Trustees of the University of Illinois.         *
 * All rights reserved.                                                      *
 *                                                                           *
 * This file is part of the HDF Java Products distribution.                  *
 * The full copyright notice, including terms governing use, modification,   *
 * and redistribution, is contained in the file, COPYING.                    *
 * COPYING can be found at the root of the source code distribution tree.    *
 * If you do not have access to this file, you may request a copy from       *
 * help@hdfgroup.org.                                                        *
 ****************************************************************************/

package hdf.hdf5lib.structs;

import java.io.Serializable;

//Information struct for object (for H5Fget_info)
public class H5F_info_t implements Serializable{
    private static final long serialVersionUID = 4691681162544054518L;
    public long        super_ext_size;    // Superblock extension size
    public long        sohm_hdr_size;       // Shared object header message header size
    public H5_ih_info_t    sohm_msgs_info;      // Shared object header message index & heap size

    public H5F_info_t (long super_ext_size, long sohm_hdr_size, H5_ih_info_t sohm_msgs_info)
    {
        this.super_ext_size = super_ext_size;
        this.sohm_hdr_size = sohm_hdr_size;
        this.sohm_msgs_info = sohm_msgs_info;
    }
}
