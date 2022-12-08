package it.govio.batch.test.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
class FileProcessingJobTest {

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;
	
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

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void csvLoadOk() throws Exception {

		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		
		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();

		govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "01"));
		govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "02"));
		govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "03"));
		govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "04"));
		govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "05"));

		System.out.println(">>>>>>>> " + govioFilesRepository.findAll().size());

		initailizeJobLauncherTestUtils();
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

	}
	
	private GovioFileEntity buildFile(TemporaryFolder t, GovioServiceInstanceEntity instanceService, String i) throws IOException {
		File file = t.newFile(i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
//		Random r = new Random();
//		int nextInt = r.nextInt(50);
		int nextInt = 50;
		for(int x=0;x<nextInt+50;x++)
			file1writer.write("XXXXXX"+i+"A00Y"+String.format("%03d", x)+"Z,2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		file1writer.close();
		
		GovioFileEntity govioFile1 = GovioFileEntity.builder()
				.creationDate(LocalDateTime.now())
				.govioServiceInstance(instanceService)
				.location(file.toPath().toString())
				.name(file.getName())
				.status(Status.CREATED)
				.build();
		
		return govioFile1;
	}


}
