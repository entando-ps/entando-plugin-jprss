/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.entando.entando.plugins.jprss.aps.system.services.rss;

import org.junit.jupiter.api.Assertions;

import com.agiletec.aps.BaseTestCase;
import com.agiletec.aps.system.common.entity.IEntityTypesConfigurer;
import com.agiletec.aps.system.common.entity.model.attribute.AttributeInterface;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import org.entando.entando.plugins.jprss.aps.system.services.JpRssSystemConstants;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestRssManager extends BaseTestCase {
	
	@Test
    public void testGetAvailableContentTypes() {
		Map<String, String> availableContentTypes = this.getRssManager().getAvailableContentTypes();
		Assertions.assertNotNull(availableContentTypes);
		Assertions.assertEquals(1, availableContentTypes.size());
		String key = "ART";
		String descr = availableContentTypes.get(key);
		Assertions.assertEquals("Articolo rassegna stampa", descr);
	}
	
	@Test
    public void testCRUDChannel() throws Throwable {
		Channel testChannel = this.createTestChannel();
		//ADD CHANNEL
		this.getRssManager().addChannel(testChannel);
		Assertions.assertTrue(testChannel.getId() > 0);
		
		//GET LIST
		List<Channel> activeList = this.getRssManager().getChannels(Channel.STATUS_ACTIVE);
		List<Channel> notActiveList = this.getRssManager().getChannels(Channel.STATUS_NOT_ACTIVE);
		List<Channel> fullList = this.getRssManager().getChannels(Channel.STATUS_ALL);
		Assertions.assertNotNull(activeList);
		Assertions.assertEquals(1, activeList.size());
		Assertions.assertNotNull(notActiveList);
		Assertions.assertEquals(0, notActiveList.size());
		Assertions.assertNotNull(fullList);
		Assertions.assertEquals(1, fullList.size());
		
		//GET ONE
		Channel channel = activeList.get(0);
		Assertions.assertEquals(channel.getCategory(), testChannel.getCategory());
		Assertions.assertEquals(channel.getContentType(), testChannel.getContentType());
		Assertions.assertEquals(channel.getDescription(), testChannel.getDescription());
		Assertions.assertEquals(channel.getFeedType(), testChannel.getFeedType());
		Assertions.assertEquals(channel.getId(), testChannel.getId());
		Assertions.assertEquals(channel.getTitle(), testChannel.getTitle());
		Assertions.assertEquals(channel.isActive(), testChannel.isActive());
		
		channel.setCategory("evento");
		channel.setDescription("updated_test channel");
		channel.setTitle("updated_test title");
		channel.setFeedType("rss_1.0");
		channel.setActive(false);
		this.getRssManager().updateChannel(channel);
		
		Channel updated = this.getRssManager().getChannel(channel.getId());
		Assertions.assertEquals(updated.getCategory(), channel.getCategory());
		Assertions.assertEquals(updated.getContentType(), channel.getContentType());
		Assertions.assertEquals(updated.getDescription(), channel.getDescription());
		Assertions.assertEquals(updated.getFeedType(), channel.getFeedType());
		Assertions.assertEquals(updated.getId(), channel.getId());
		Assertions.assertEquals(updated.getTitle(), channel.getTitle());		
		Assertions.assertEquals(updated.isActive(), channel.isActive());
		
		notActiveList = this.getRssManager().getChannels(Channel.STATUS_NOT_ACTIVE);
		activeList = this.getRssManager().getChannels(Channel.STATUS_ACTIVE);
		
		Assertions.assertTrue(notActiveList.size() == 1);
		Assertions.assertTrue(activeList.isEmpty());
		
		this.getRssManager().deleteChannel(updated.getId());
		activeList = this.getRssManager().getChannels(Channel.STATUS_ACTIVE);
		notActiveList = this.getRssManager().getChannels(Channel.STATUS_NOT_ACTIVE);
		fullList = this.getRssManager().getChannels(Channel.STATUS_ALL);
		Assertions.assertTrue(activeList.isEmpty());
		Assertions.assertTrue(notActiveList.isEmpty());
		Assertions.assertTrue(fullList.isEmpty());
	}
	
	private Channel createTestChannel() {
		Channel channel = new Channel();
		channel.setActive(true);
		channel.setCategory("cat1");
		channel.setContentType("ART");
		channel.setDescription("test channel");
		channel.setFeedType("rss_2.0");
		channel.setTitle("test title");
		return channel;
	}
	
    @BeforeAll
	private void init() throws Exception {
		try {
			this._rssManager = (IRssManager) this.getService(JpRssSystemConstants.RSS_MANAGER);
			IContentManager contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
			Content contentType = contentManager.createContentType("ART");
			AttributeInterface titolo = (AttributeInterface) contentType.getAttribute("Titolo");
			String[] roles = {JpRssSystemConstants.ATTRIBUTE_ROLE_RSS_CONTENT_TITLE};
			titolo.setRoles(roles);
			((IEntityTypesConfigurer) contentManager).updateEntityPrototype(contentType);
			this.waitNotifyingThread();
		} catch (Throwable t) {
			throw new Exception(t);
		}
	}
	
	@AfterAll
	protected void tearDownEach() throws Exception {
		try {
			List<Channel> fullList = this.getRssManager().getChannels(Channel.STATUS_ALL);
			Iterator<Channel> it = fullList.iterator();
			while (it.hasNext()) {
				Channel current = it.next();
				int id = current.getId();
				this.getRssManager().deleteChannel(id);
			}
			fullList = this.getRssManager().getChannels(Channel.STATUS_ALL);
			Assertions.assertTrue(fullList.isEmpty());
			IContentManager contentManager = (IContentManager) this.getService(JacmsSystemConstants.CONTENT_MANAGER);
			Content contentType = contentManager.createContentType("ART");
			AttributeInterface titolo = (AttributeInterface) contentType.getAttribute("Titolo");
			String[] roles = {JpRssSystemConstants.ATTRIBUTE_ROLE_RSS_CONTENT_TITLE};
			titolo.setRoles(roles);
			((IEntityTypesConfigurer) contentManager).updateEntityPrototype(contentType);
			this.waitNotifyingThread();
		} catch (Throwable t) {
			throw new Exception(t);
		} finally {
			super.tearDown();
		}
	}
	
	public IRssManager getRssManager() {
		return _rssManager;
	}
	
	private IRssManager _rssManager;
	
}