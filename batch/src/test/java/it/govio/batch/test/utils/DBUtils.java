/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govio.batch.test.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.dao.DataAccessResourceFailureException;

public class DBUtils {
	
	private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

	public static void stopH2Database() throws SQLException {
	    Connection conn = DriverManager
	            .getConnection("jdbc:h2:file:/tmp/govio-batch-db", "sa", "");
	     Statement stat = conn.createStatement();
	     stat.executeUpdate("SHUTDOWN");
	     stat.close();
	     conn.close();
	}
	
	public static void clearSpringBatchTables() throws SQLException {
		
		String query = "DELETE FROM BATCH_JOB_EXECUTION_PARAMS;\n"
				+ "DELETE FROM  BATCH_JOB_EXECUTION_CONTEXT;\n"
				+ "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT;\n"
				+ "DELETE FROM BATCH_STEP_EXECUTION;\n"
				+ "DELETE FROM BATCH_JOB_EXECUTION;\n"
				+ "DELETE FROM BATCH_JOB_INSTANCE;";
		
	    Connection conn = DriverManager
	            .getConnection("jdbc:h2:file:/tmp/govio-batch-db", "sa", "");
	     Statement stat = conn.createStatement();
	     stat.executeUpdate(query);
	     stat.close();
	     conn.close();
		
	}
	

	// TODO: Questa va in un bean con il jobExplorer istanziato, e l'argomento 
	// jobName non è necessario, recupero tutti i job e se ci riesco allora il db è up.
	// Inoltre ci va un'exponential backoff perchè sembra che ogni volta che l'eccezione viene sollevata,
	// il processo di restart del db si ripete e possiamo rimanre bloccati nel while.
	public static void awaitForDb(JobExplorer jobExplorer, String jobName) {
		JobInstance instance = null;
		while (instance == null) {
			try {
				instance = jobExplorer.getLastJobInstance(jobName);
			} catch (DataAccessResourceFailureException e) {
				log.info("Aspetto ancora per il db...");
				sleep(500);
			}
		}
	}

	public static void awaitAllCurrentJobs(JobExplorer jobExplorer) throws InterruptedException {
		
		for (String jn : jobExplorer.getJobNames()) {             
			   for (JobExecution je : jobExplorer.findRunningJobExecutions(jn)) {
				   while (je.isRunning()) {
						je = jobExplorer.getJobExecution(je.getId());
						Thread.sleep(20);
				   }
			   }
		}
	}
	
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
