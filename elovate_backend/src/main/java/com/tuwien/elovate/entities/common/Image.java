package com.tuwien.elovate.entities.common;

/*-
 * #%L
 * ELOvate
 * %%
 * Copyright (C) 2024 ELOvate GmbH.
 * %%
 * Copyright (C) 2024 ELOvate GmbH. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * #L%
 */

import io.micrometer.common.lang.NonNullApi;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "image")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NonNullApi
public class Image implements MultipartFile {

    private static final int MB_SIZE_IN_BYTES = 1024 * 1024;
    private static final int MAX_FILE_SIZE_IN_BYTES = 2 * MB_SIZE_IN_BYTES;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Lob
    @Size(max = MAX_FILE_SIZE_IN_BYTES)
    private byte[] fileContent;

    @ToString.Include
    private String name;

    @ToString.Include
    private String originalFilename;

    @ToString.Include
    private String contentType;

    private boolean isEmpty;

    private long size;

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            fileOutputStream.write(this.fileContent);
        }
    }

    public String getImageBase64() {
        String base64String = Base64.getEncoder().encodeToString(fileContent);
        return "data:" + contentType + ";base64," + base64String;
    }
}
