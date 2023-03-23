package it.govhub.govio.api.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileEntity.Status;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;

public class GovioFileUtils {

	public static GovioFileEntity buildFile(Path fileRepositoryPath, GovioServiceInstanceEntity instanceService, String i, UserEntity user) throws IOException {
		Path destPath = fileRepositoryPath
				.resolve(instanceService.getOrganization().getId().toString())
				.resolve(instanceService.getService().getId().toString());
		
		File destDir = destPath.toFile();
    	destDir.mkdirs();
		
		File file = new File(destDir, i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++)
			file1writer.write("XXXXXX"+i+"A00Y"+String.format("%03d", x)+"Z,2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		file1writer.close();
    	
    	Path destFile =  destPath.resolve(file.getName());

		GovioFileEntity govioFile1 = GovioFileEntity.builder()
				.creationDate(OffsetDateTime.now())
				.serviceInstance(instanceService)
				.location(destFile)
				.name(file.getName())
				.status(Status.CREATED)
				.govauthUser(user)
				.build();

		return govioFile1;
	}
	
	
	public static String createApiKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
