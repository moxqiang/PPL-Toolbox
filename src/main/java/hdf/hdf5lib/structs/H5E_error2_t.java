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

//Information struct for Attribute (For H5Ewalk)
public class H5E_error2_t implements Serializable{
    private static final long serialVersionUID = 279144359041667613L;

    public int   cls_id;     //class ID
    public int   maj_num;      //major error ID
    public int   min_num;      //minor error number
    public int    line;          //line in file where error occurs
    public String func_name;  //function in which error occurred
    public String file_name;  //file in which error occurred
    public String desc;          //optional supplied description

    H5E_error2_t(int cls_id, int maj_num, int min_num, int line, String func_name, String file_name, String desc) {
        this.cls_id = cls_id;
        this.maj_num = maj_num;
        this.min_num = min_num;
        this.line = line;
        this.func_name = func_name;
        this.file_name = file_name;
        this.desc = desc;
    }
}
