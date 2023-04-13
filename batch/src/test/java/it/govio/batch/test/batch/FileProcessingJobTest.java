/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
package it.govio.batch.test.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;

@SpringBootTest(/*classes = Application.class*/)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class FileProcessingJobTest {

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;

	@Autowired
	private GovioFilesRepository govioFileMessagesRepository;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;

	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = "FileProcessingJob")
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;

	private void initailizeJobLauncherTestUtils() throws Exception { 
		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
	}

	@BeforeAll
	public void initTest() throws SQLException {
		Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082")
		.start();
	}

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
		
		
	}

	/**
	 * Test di elaborazione con successo di una serie di tracciati CSV
	 * @throws Exception
	 */
	@Test
	void csvLoadOk() throws Exception {

		assertEquals(0, govioMessagesRepository.count());
		
		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);

		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();

		// Inserisco 5 file con 100 record ciascuna
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "01")));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "02")));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "03")));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "04")));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "05")));

		initailizeJobLauncherTestUtils();

		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

		for(GovioFileEntity entity : govioFilesRepository.findAll()) {
			// Controllo lo stato di elaborazione
			assertEquals(Status.PROCESSED, entity.getStatus());
			assertEquals(100, entity.getAcquiredMessages());
			assertEquals(0, entity.getErrorMessages());
		}

		assertEquals(500, govioMessagesRepository.count());

		for(GovioMessageEntity entity : govioMessagesRepository.findAll()) {
			assertEquals(GovioMessageEntity.Status.SCHEDULED, entity.getStatus());
		}

	}

	private GovioFileEntity buildFile(TemporaryFolder t, GovioServiceInstanceEntity instanceService, String i) throws IOException {
		File file = t.newFile(i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++)
			file1writer.write("XXXXXX"+i+"A00Y"+String.format("%03d", x)+"Z,2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		file1writer.close();

		GovioFileEntity govioFile1 = GovioFileEntity.builder()
				.creationDate(LocalDateTime.now())
				.govioServiceInstance(instanceService)
				.govhubUserId(1l)
				.location(file.toPath().toString())
				.name(file.getName())
				.status(Status.CREATED)
				.build();

		return govioFile1;
	}
	
}
