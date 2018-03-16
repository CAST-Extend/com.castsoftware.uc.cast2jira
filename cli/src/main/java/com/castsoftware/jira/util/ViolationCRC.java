package com.castsoftware.jira.util;

import java.util.zip.CRC32;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class ViolationCRC calculates the CastId to ensure that we do not insert
 * in Jira duplicate issues.
 * 
 * @author FME
 * @version 1.1
 */

public class ViolationCRC {

	/** The log. */
	public static Log log = LogFactory.getLog(SqlStatements.class);

	/** The record. */
	private String record;

	/**
	 * Instantiates a new violation crc.
	 */
	public ViolationCRC() {
		// this.record = record;
	}

	/**
	 * Sets the hash code.
	 * 
	 * @param record
	 *            the new hash code
	 */
	public void setHashCode(String record) {
		this.record = record;

	}

	/**
	 * Gets the hash code.
	 * 
	 * @return the hash code
	 * @throws Exception
	 *             the exception
	 */
	public int getHashCode() throws Exception {

		// get bytes from string
		byte bytes[] = this.record.getBytes();

		CRC32 crc = new CRC32();

		// update the current checksum with the specified array of bytes
		crc.update(bytes, 0, bytes.length);

		log.debug("CRC32 checksum for input string is: " + crc.getValue());

		// get the current checksum value
		return (int) (crc.getValue());
	}
}
