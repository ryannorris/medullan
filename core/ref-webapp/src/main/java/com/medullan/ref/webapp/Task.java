package com.medullan.ref.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import com.medullan.ref.webapp.component.Car;
import java.util.Date;

@Controller
public class Task {
	public Car car;
	
	@RequestMapping("/task")
	public String task(Model model) {
		
		///return "home";
		return "home";
	}
	
}
