package com.jaceksysiak.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jaceksysiak.App;
import com.jaceksysiak.model.Interest;
import com.jaceksysiak.model.Profile;
import com.jaceksysiak.model.SiteUser;
import com.jaceksysiak.service.InterestService;
import com.jaceksysiak.service.ProfileService;
import com.jaceksysiak.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(App.class)
@WebAppConfiguration
@Transactional
public class ProfileControllerRestTest {

	@Autowired
	private UserService userService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private InterestService interestService;

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@WithMockUser(username = "test@wp.pl") 
	public void testSaveAndDeleteInterest() throws Exception {
		
		SiteUser user = new SiteUser("test@wp.pl", "test");
		userService.register(user);
		Profile profile = new Profile(user);
		
 		String interestText = "some interest_here";
 		Interest interest = interestService.createIfNotExists(interestText);
		HashSet<Interest> interestSet = new HashSet<>();
		interestSet.add(interest);
		
 
		profile.setInterests(interestSet);
		profileService.save(profile);
		
		mockMvc.perform(post("/save-interest").param("name", interestText)).andExpect(status().isOk());
		
		Interest retInterest = interestService.get(interestText);
		
		assertNotNull("Interest should exist", interest);
		assertEquals("Retrieved interest text should match", interestText, retInterest.getName());
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		SiteUser user2 = userService.get(email);
		
		Profile profile2 = profileService.getUserProfile(user2);
		
	
		
		assertTrue("Profile should contain interest", profile2.getInterests().contains(new Interest(interestText)));

		mockMvc.perform(post("/delete-interest").param("name", interestText)).andExpect(status().isOk());

		profile2 = profileService.getUserProfile(user2);

		assertFalse("Profile should not contain interest", profile.getInterests().contains(new Interest(interestText)));
		
	}
}
 




































