/**
 * 
 */
package com.medullan.ref.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.medullan.ref.webapp.component.Car;

/**
 * @author rnorris
 *
 */
@Controller
public class WelcomeController {

	public Car car;
	
	@RequestMapping("/hello")
	public String helloWorld(Model model) {
	//	car.startEngine();
		return "home";
	}
	
	@Autowired
	public void setCar(Car car) {
		this.car = car;
	}
	
	public Car getCar() {
		return car;
	}
	
}
