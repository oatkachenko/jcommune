/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.web.controller;

import org.jtalks.jcommune.model.entity.Branch;
import org.jtalks.jcommune.model.entity.CodeReviewComment;
import org.jtalks.jcommune.model.entity.JCUser;
import org.jtalks.jcommune.service.*;
import org.jtalks.jcommune.service.exceptions.NotFoundException;
import org.jtalks.jcommune.web.dto.CodeReviewCommentDto;
import org.jtalks.jcommune.web.dto.json.FailValidationJsonResponse;
import org.jtalks.jcommune.web.dto.json.JsonResponse;
import org.jtalks.jcommune.web.dto.json.JsonResponseStatus;
import org.mockito.Mock;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

/**
 * @author Vyacheslav Mishcheryakov
 */
public class CodeReviewCommentControllerTest {
    public long BRANCH_ID = 1L;
    
    private long COMMENT_ID = 1L;
    private String COMMENT_BODY = "body";
    private int COMMENT_LINE_NUMBER = 1;
    
    private long USER_ID = 1L;
    private String USERNAME = "username";

    private Branch branch;

    @Mock
    private CodeReviewService codeReviewService;
    @Mock
    private CodeReviewCommentService codeReviewCommentService;
    

    private CodeReviewCommentController controller;

    @BeforeMethod
    public void initEnvironment() {
        initMocks(this);
        controller = new CodeReviewCommentController(
                codeReviewService,
                codeReviewCommentService);
    }

    @BeforeMethod
    public void prepareTestData() {
        branch = new Branch("", "description");
        branch.setId(BRANCH_ID);
    }

    @Test
    public void testInitBinder() {
        WebDataBinder binder = mock(WebDataBinder.class);
        controller.initBinder(binder);
        verify(binder).registerCustomEditor(eq(String.class), any(StringTrimmerEditor.class));
    }

    @Test
    public void testAddCommentSuccess() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewService.addComment(anyLong(), anyInt(), anyString()))
            .thenReturn(createComment());
        
        JsonResponse response = controller.addComment(
                new CodeReviewCommentDto(), bindingResult, 1L);
        
        CodeReviewCommentDto dto = (CodeReviewCommentDto) response.getResult();
        
        assertEquals(response.getStatus(), JsonResponseStatus.Success);
        assertEquals(dto.getId(), COMMENT_ID);
        assertEquals(dto.getBody(), COMMENT_BODY);
        assertEquals(dto.getLineNumber(), COMMENT_LINE_NUMBER);
        assertEquals(dto.getAuthorId(), USER_ID);
        assertEquals(dto.getAuthorUsername(), USERNAME);
    }
    
    @Test
    public void testAddCommentValidationFail() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(true);
        
        FailValidationJsonResponse response = (FailValidationJsonResponse)controller
                .addComment(new CodeReviewCommentDto(), bindingResult, 1L);
        
        assertNotNull(response.getResult());
    }
    
    @Test(expectedExceptions=NotFoundException.class)
    public void testAddCommentReviewNotFound() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewService.addComment(anyLong(), anyInt(), anyString()))
            .thenThrow(new NotFoundException());
        
        controller.addComment(new CodeReviewCommentDto(), bindingResult, 1L);
    }
    
    @Test(expectedExceptions=AccessDeniedException.class)
    public void testAddCommentAccessDenied() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewService.addComment(anyLong(), anyInt(), anyString()))
            .thenThrow(new AccessDeniedException(null));
        
        controller.addComment(new CodeReviewCommentDto(), bindingResult, 1L);
    }
    
    @Test
    public void testEditCommentSuccess() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewCommentService.updateComment(anyLong(), anyString()))
            .thenReturn(createComment());
        
        JsonResponse response = controller.editComment(
                new CodeReviewCommentDto(), bindingResult);
        
        CodeReviewCommentDto dto = (CodeReviewCommentDto) response.getResult();
        
        assertEquals(response.getStatus(), JsonResponseStatus.Success);
        assertEquals(dto.getId(), COMMENT_ID);
        assertEquals(dto.getBody(), COMMENT_BODY);
        assertEquals(dto.getLineNumber(), COMMENT_LINE_NUMBER);
        assertEquals(dto.getAuthorId(), USER_ID);
        assertEquals(dto.getAuthorUsername(), USERNAME);
    }
    
    @Test
    public void testEditCommentValidationFail() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(true);
        
        FailValidationJsonResponse response = (FailValidationJsonResponse)controller
                .editComment(new CodeReviewCommentDto(), bindingResult);
        
        assertNotNull(response.getResult());
    }
    
    @Test(expectedExceptions=NotFoundException.class)
    public void testEditCommentNotFound() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewCommentService.updateComment(anyLong(), anyString()))
            .thenThrow(new NotFoundException());
        
        controller.editComment(new CodeReviewCommentDto(), bindingResult);
    }
    
    @Test(expectedExceptions=AccessDeniedException.class)
    public void testEditCommentAccessDenied() throws AccessDeniedException, NotFoundException {
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(bindingResult.hasErrors()).thenReturn(false);
        when(codeReviewCommentService.updateComment(anyLong(), anyString()))
            .thenThrow(new AccessDeniedException(null));
        
        controller.editComment(new CodeReviewCommentDto(), bindingResult);
    }
    
    private CodeReviewComment createComment() {
        CodeReviewComment comment = new CodeReviewComment();
        comment.setId(COMMENT_ID);
        comment.setBody(COMMENT_BODY);
        comment.setLineNumber(COMMENT_LINE_NUMBER);
        
        JCUser user = new JCUser(USERNAME, null, null);
        user.setId(USER_ID);
        comment.setAuthor(user);
        
        return comment;
    }
    
}
