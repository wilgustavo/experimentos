package com.demo.batch.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class BookImportStepListener implements StepExecutionListener {

    Logger logger = LoggerFactory.getLogger(BookImportStepListener.class);

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("{} books have been imported", stepExecution.getWriteCount());
        if (stepExecution.getSkipCount() > 0) {
            logger.info("There have been {} books with errors that could not be imported.", stepExecution.getSkipCount());
        }
        return stepExecution.getExitStatus();
    }
}
