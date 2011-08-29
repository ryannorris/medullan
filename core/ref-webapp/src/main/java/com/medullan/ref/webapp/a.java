package com.medullan.ref.webapp;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.medullan.ref.webapp.WelcomeController;

import junit.framework.TestCase;
import org.springframework.ui.Model;

public class a extends TestCase{

	@Test
    public void testHandleRequestView() throws Exception{		
		Task controller = new Task();
        String a = controller.task( null );		
        assertEquals("home", a);
    }

}
