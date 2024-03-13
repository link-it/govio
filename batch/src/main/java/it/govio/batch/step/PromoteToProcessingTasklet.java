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
package it.govio.batch.step;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.repository.GovioFilesRepository;

@Component
public class PromoteToProcessingTasklet implements Tasklet {

	private Logger logger = LoggerFactory.getLogger(PromoteToProcessingTasklet.class);
	
	@Autowired
	private GovioFilesRepository repository;
	
	@Value("${jobs.FileProcessingJob.steps.govioFileReaderMasterStep.partitioner.grid-size:10}")
	Integer gridSize;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<GovioFileEntity> processingFiles = repository.findByStatus(Status.PROCESSING, null);
		int newFiles = gridSize - processingFiles.size();		
		if (processingFiles.size() > 0) {
			logger.warn("Trovati dei file in stato {} provenienti da una passata esecuzione fallita, li aggiungo al batch.", Status.PROCESSING);
		}
		if (processingFiles.size() > gridSize) {
			logger.error("Trovati più di {} file in stato {}, potrebbe rompersi lo scheduler del FileProcessingJob.", gridSize, Status.PROCESSING);
		}
		if (newFiles > 0) {
			List<GovioFileEntity> toProcess = repository.findByStatus(Status.CREATED, PageRequest.of(0, newFiles, Sort.by("id")));
			processingFiles.addAll(toProcess);
		}
		
		List<Long> ids = processingFiles.stream()
				.map(GovioFileEntity::getId)
				.collect(Collectors.toList());
		
		//int updateAllStatus = repository.updateAllStatus(Status.CREATED, Status.PROCESSING);
		int updateAllStatus = repository.updateStatus(ids, Status.PROCESSING);
		
		logger.info("Promoting Files from CREATED to PROCESSING status");
		if(updateAllStatus>0) {
			logger.info("Promoted {} files to PROCESSING status", updateAllStatus);
			contribution.setExitStatus(new ExitStatus("NEW_FILES_FOUND"));  
		} else {
			contribution.setExitStatus(new ExitStatus("NEW_FILES_NOT_FOUND"));  
		}
		return RepeatStatus.FINISHED;
	}
}
