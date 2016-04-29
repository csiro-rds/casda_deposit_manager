package au.csiro.casda.dto;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */


import java.io.Serializable;

import au.csiro.casda.entity.ValidationNote;

/**
 * Data transfer object for validation note.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ValidationNoteDTO implements Serializable
{
    private static final long serialVersionUID = 3091348587098564975L;
    private long id;
    private String userId;
    private String userName;
    private String content;
    private Long createdDate;

    /**
     * Empty constructor for JSON serialisation.
     */
    public ValidationNoteDTO()
    {
    }

    /**
     * Constructor from validation note
     * 
     * @param validationNote
     *            the validation note database record 
     */
    public ValidationNoteDTO(ValidationNote validationNote)
    {
        this.id = validationNote.getId();
        this.userId = validationNote.getPersonId();
        this.userName = validationNote.getPersonName();
        this.content = validationNote.getContent();
        this.createdDate = validationNote.getCreated().getMillis();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Long getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate)
    {
        this.createdDate = createdDate;
    }

}
