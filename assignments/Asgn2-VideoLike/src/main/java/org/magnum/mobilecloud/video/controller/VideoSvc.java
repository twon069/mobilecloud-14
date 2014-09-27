package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoSvc {

	@Autowired
	private VideoRepository videoRepo;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videoRepo.findAll());
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		return videoRepo.findOne(id);
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		return videoRepo.save(v);
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return videoRepo.findByName(title);
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videoRepo.findByDurationLessThan(duration);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
	public Void likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		Video v = videoRepo.findOne(id);
		if (null == v)
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		else
		{
			String username = p.getName();
			Collection<String> likedUsers = v.getLikedUsers();
			
			if (likedUsers.contains(username))
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			else
			{
				response.setStatus(HttpServletResponse.SC_OK);
				likedUsers.add(username);
				v.setLikedUsers(likedUsers);
				v.setLikes(v.getLikes() + 1);
				videoRepo.save(v);
			}
		}
		return null;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public Void unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		Video v = videoRepo.findOne(id);
		if (null == v)
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);;
		}
		else
		{
			String username = p.getName();
			Collection<String> likedUsers = v.getLikedUsers();
			
			if (!likedUsers.contains(username))
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			else
			{
				response.setStatus(HttpServletResponse.SC_OK);
				likedUsers.remove(username);
				v.setLikedUsers(likedUsers);
				v.setLikes(v.getLikes() - 1);
				videoRepo.save(v);
			}
		}
		return null;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id) {
		Video v = videoRepo.findOne(id);
		if (null == v)
			throw new ResourceNotFoundException();
		return v.getLikedUsers();
	}
}
