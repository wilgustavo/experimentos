package com.demo.batch.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class BooksJob {

    Logger logger = LoggerFactory.getLogger(BooksJob.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BooksJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    private ConversionService localDateConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        DefaultConversionService.addDefaultConverters(conversionService);
        conversionService.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String text) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
                return LocalDate.parse(text, formatter);
            }
        });
        return conversionService;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Book> booksCsvReader(@Value("#{jobParameters['path']}") String path) {

        BeanWrapperFieldSetMapper<Book> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Book.class);
        mapper.setConversionService(localDateConversionService());

        return new FlatFileItemReaderBuilder<Book>()
                .name("bookReader")
                .resource(new FileSystemResource(path))
                .linesToSkip(1)
                .delimited()
                .names("id","title","authors","average_rating","isbn","isbn13","language_code","num_pages",
                        "ratings_count","text_reviews_count","publication_date","publisher")
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public ItemProcessor<Book, BookEntity> bookCsvProcessor() {
        return BookEntity::fromBook;
    }

    @Bean
    public JdbcBatchItemWriter<BookEntity> bookJdbcWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BookEntity>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO BOOK(BOOK_ID, TITLE, AUTHORS, ISBN13, PUBLICATION, PUBLISHER)
                        VALUES (:id, :title, :authors, :isbn, :publication, :publisher)
                     """)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }


    @Bean
    public Step bookImportStep(FlatFileItemReader<Book> booksCsvReader,
                               ItemProcessor<Book, BookEntity> bookCsvProcessor,
                               JdbcBatchItemWriter<BookEntity> bookJdbcWriter) {
        return new StepBuilder("import-book-step", jobRepository)
                .<Book, BookEntity>chunk(10, transactionManager)
                .reader(booksCsvReader)
                .processor(bookCsvProcessor)
                .writer(bookJdbcWriter)
                .faultTolerant()
                .skipLimit(10)
                .skip(FlatFileParseException.class)
                .listener(new BookImportStepListener())
                .build();
    }

    @Bean
    public Job bookImportJob(Step bookImportStep) {
        return new JobBuilder("import-book", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(bookImportStep)
                .build();
    }

}
