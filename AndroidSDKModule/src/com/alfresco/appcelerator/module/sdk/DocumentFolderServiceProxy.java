/*
 ******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *****************************************************************************
 */

package com.alfresco.appcelerator.module.sdk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.Log;


@SuppressWarnings("deprecation")
@Kroll.proxy(creatableInModule = AndroidsdkmoduleModule.class)
public class DocumentFolderServiceProxy extends KrollProxy
{
	private DocumentFolderService service;
	private Folder currentFolder;
    
    
    public DocumentFolderServiceProxy() 
    {
		super();
	}
    
    
    @Kroll.method
    void initialiseWithSession(Object[] args)
    {
    	SessionProxy seshProxy = (SessionProxy) args[0];
        service = seshProxy.session.getServiceRegistry().getDocumentFolderService();
    }
    
    
    @Kroll.method
    void retrieveRootFolder()
    {
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			currentFolder = service.getRootFolder();
    			
    			HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("folder", currentFolder.getName());
    	        fireEvent("retrievedfolder", new KrollDict(map));
    		}
    	}.start();
    }
    
    
    @Kroll.method
    void setFolder(Object[] args)
    {
    	FolderProxy folder = (FolderProxy)args[0];
    	
    	currentFolder = folder.getFolder();
    }
    
    
    @Kroll.method
    void retrieveChildrenInFolder()
    {
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			List<Node> nodes;
    			try
    			{
	    			// Get children of document library
	    	        nodes = service.getChildren(currentFolder);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getChildren()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    	        Log.i("Alfresco", "Nodes: " + nodes.size());
    	        
    	        for (Node node : nodes)
    	        {
    	        	SDKUtil.createEventWithNode (node, DocumentFolderServiceProxy.this);
    	        }
    	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveChildrenInFolder", null);
    	        
    			super.run();
    		}
    	}.start();
    }
    
    
    /** Retrieve children in current folder (either root after retrieveRootFolder(), or a specific folder after a setFolder() call).
    Result event will include listing context for paged handling.
    @since v1.0
    */
    @Kroll.method
    void retrieveChildrenInFolderWithListingContext(Object[] arg)
    {
    	final ListingContextProxy lc = (ListingContextProxy)arg[0];
    	
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			PagingResult<Node> nodes;
    			try
    			{
	    			// Get children of document library
	    	        nodes = service.getChildren(currentFolder, lc.listingContext);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getChildren()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    	        Log.i("Alfresco", "Nodes: " + nodes.getList().size());
    	        
    	        for (Node node : nodes.getList())
    	        {
    	        	SDKUtil.createEventWithNode (node, DocumentFolderServiceProxy.this);
    	        }
    	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveChildrenInFolderWithListingContext", null);
    	        SDKUtil.createEventWithPagingResult (nodes, DocumentFolderServiceProxy.this);
    	        
    			super.run();
    		}
    	}.start();
    }
    
   
    @Kroll.method
    FolderProxy getCurrentFolder()
    {
    	return new FolderProxy (currentFolder);
    }
    
 
    @Kroll.method
    void retrievePermissionsOfNode (Object[] arg)
    {
    	final NodeProxy nodeProxy = (NodeProxy)arg[0];
    	
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			Permissions permissions = service.getPermissions (nodeProxy.node);
    	        
    			PermissionsProxy p = new PermissionsProxy(permissions);
    	        HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("permissions", p);
    	        fireEvent("retrievedpermissions", new KrollDict(map) );
    		}
    	}.start();
    }


    //Deprecated in favour of retrieveContentOfDocument method.
    @Kroll.method
    void saveDocument(final Object arg[])
    {
        retrieveContentOfDocument(arg);
    }


    @Kroll.method
    void retrieveContentOfDocument (final Object args[])
    {
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			DocumentProxy arg = (DocumentProxy) args[0];
    		     
    	        ContentFile file = service.getContent(arg.getDocument());
    	        if (!file.getFile().exists())
    	        {
    	        	SDKUtil.createErrorEventWithCode (SDKUtil.ERROR_CODE_FILE_NOT_FOUND, "File does not exist", DocumentFolderServiceProxy.this);
    	        	return;
    	        }
    	        
    	        HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("contentfile", new ContentFileProxy(file, arg.getDocument().getName()));
    	        fireEvent("retrieveddocument", new KrollDict(map) );
    		}
    	}.start();
    }
    
    
    @Kroll.method
    void createDocumentWithName (final Object args[])
    {
    	new Thread()
    	{
    		@SuppressWarnings({ "unchecked" })
			@Override
    		public void run() 
    		{
    			String name = (String)args[0];
    			FolderProxy folderProxy = (FolderProxy)args[1];
			    ContentFileProxy fileProxy = (ContentFileProxy)args[2];
			    HashMap<String,Serializable> nodeProperties = (HashMap<String,Serializable>)args[3];			    
			    Document doc;
			    
    			try
    			{
    				doc = service.createDocument (folderProxy.getFolder(), name, nodeProperties, fileProxy.contentFile);  
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.createDocument()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    	        SDKUtil.createEventWithNode (doc, DocumentFolderServiceProxy.this, "newdocumentnode"); 
    		}
    	}.start();
    }
    
    
    @Kroll.method
    void createFolderWithName (final Object args[])
    {
    	new Thread()
    	{
    		@SuppressWarnings({ "unchecked" })
			@Override
    		public void run() 
    		{
    			String name = (String)args[0];
    			FolderProxy folderProxy = (FolderProxy)args[1];
    			HashMap<String,Serializable> nodeProperties = (HashMap<String,Serializable>)args[2];		    
			    Folder folder;
			    
    			try
    			{
    				folder = service.createFolder (folderProxy.getFolder(), name, nodeProperties);  
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.createDocument()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    	        SDKUtil.createEventWithNode (folder, DocumentFolderServiceProxy.this, "newfoldernode"); 
    		}
    	}.start();
    }
    
    
    @Kroll.method
    void deleteNode (final Object args[])
    {
    	new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			NodeProxy arg = (NodeProxy) args[0];
    		     
    			try
    			{
    				service.deleteNode(arg.node);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.deleteNode()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    	        HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("code", 0);
    	        fireEvent("deletednode", new KrollDict(map) );
    		}
    	}.start();
    }
    
    

	/** Retrieves a list of favorite documents for current user .
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteDocuments(Object[] noargs)
	{
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			List<Document> docs;
    			
    			try
    			{
    				docs = service.getFavoriteDocuments();
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteDocuments()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Document doc : docs)
     	        {
     	        	SDKUtil.createEventWithNode (doc, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteDocuments", null);
    		}
    	}.start();
	}
	
	
	/** Retrieves a list of favorite documents with a listing context for current user.
	 @param listingContext The listing context with a paging definition that's used to retrieve favorite documents.
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteDocumentsWithListingContext(Object[] arg)
	{
		final ListingContextProxy lc = (ListingContextProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			PagingResult<Document> docs;
    			
    			try
    			{
    				docs = service.getFavoriteDocuments (lc.listingContext);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteDocuments()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Document doc : docs.getList())
     	        {
     	        	SDKUtil.createEventWithNode (doc, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteDocumentsWithListingContext", null);
     	        SDKUtil.createEventWithPagingResult (docs, DocumentFolderServiceProxy.this);
    		}
    	}.start();
	}
	
	
	/** Retrieves a list of favorite folders for current user.
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteFolders(Object[] noargs)
	{
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			List<Folder> folders;
    			
    			try
    			{
    				folders = service.getFavoriteFolders();
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteFolders()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Folder folder : folders)
     	        {
     	        	SDKUtil.createEventWithNode (folder, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteFolders", null);
    		}
    	}.start();
	}
	
	
	/** Retrieves a list of favorite folders with a listing context for current user.
	 @param listingContext The listing context with a paging definition that's used to retrieve favorite folders.
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteFoldersWithListingContext(Object[] arg)
	{
		final ListingContextProxy lc = (ListingContextProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			PagingResult<Folder> folders;
    			
    			try
    			{
    				folders = service.getFavoriteFolders (lc.listingContext);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteFolders()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Folder folder : folders.getList())
     	        {
     	        	SDKUtil.createEventWithNode (folder, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteFoldersWithListingContext", null);
     	        SDKUtil.createEventWithPagingResult (folders, DocumentFolderServiceProxy.this);
    		}
    	}.start();
	}
	
	
	/** Retrieves a list of favorite nodes for current user.
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteNodes(Object[] noargs)
	{
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			List<Node> nodes;
    			
    			try
    			{
    				nodes = service.getFavoriteNodes();
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteNodes()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Node node : nodes)
     	        {
     	        	SDKUtil.createEventWithNode (node, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteNodes", null);
    		}
    	}.start();
	}
	
	
	/** Retrieves a list of favorite nodes with a listing context for current user.
	 @param listingContext The listing context with a paging definition that's used to retrieve favorite nodes.
	 @since v1.2
	 */
	@Kroll.method
	void retrieveFavoriteNodesWithListingContext(Object[] arg)
	{
		final ListingContextProxy lc = (ListingContextProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			PagingResult<Node> nodes;
    			
    			try
    			{
    				nodes = service.getFavoriteNodes (lc.listingContext);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.getFavoriteNodes()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			for (Node node : nodes.getList())
     	        {
     	        	SDKUtil.createEventWithNode (node, DocumentFolderServiceProxy.this);
     	        }
     	        SDKUtil.createEnumerationEndEvent (DocumentFolderServiceProxy.this, "retrieveFavoriteNodesWithListingContext", null);
    		}
    	}.start();
	}
	
	
	/** Determine whether given node is favorite.
	 @param node The node for which favorite status is being determined
	 @since v1.2
	 */
	@Kroll.method
	void isFavorite(Object[] arg)
	{
		final NodeProxy nodeProxy = (NodeProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			boolean isFavourite;
    			
    			try
    			{
    				isFavourite = service.isFavorite (nodeProxy.node);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.isFavorite()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("favorite", isFavourite ? 1 : 0);
    	        map.put("node", nodeProxy);
    	        fireEvent("retrievedisfavorite", new KrollDict(map) );
    		}
    	}.start();
	}
	
	
	/** Favorite a node.
	 @param node The node which is to be favorited
	 @since v1.2
	 */
	@Kroll.method
	void addFavorite(Object[] arg)
	{
		final NodeProxy nodeProxy = (NodeProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			try
    			{
    				service.addFavorite (nodeProxy.node);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.addFavorite()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("node", nodeProxy);
    	        fireEvent("addedfavorite", new KrollDict(map) );
    		}
    	}.start();
	}
	
	
	/** UnFavorite a node.
	 @param node The node which is to be unfavorited
	 @since v1.2
	 */
	@Kroll.method
	void removeFavorite(Object[] arg)
	{
		final NodeProxy nodeProxy = (NodeProxy)arg[0];
		
		new Thread()
    	{
    		@Override
    		public void run() 
    		{
    			try
    			{
    				service.removeFavorite (nodeProxy.node);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    				
    				SDKUtil.createErrorEvent (e, "DocumentFolderService.removeFavorite()", DocumentFolderServiceProxy.this);
                    return;
    			}
    			
    			HashMap<String, Object> map = new HashMap<String, Object>();
    	        map.put("node", nodeProxy);
    	        fireEvent("removedfavorite", new KrollDict(map) );
    		}
    	}.start();
	}
	
	
	/**
	 clears the Favorites cache
	 @since v1.2
	 */
	@Kroll.method
	void clearFavoritesCache(Object[] noargs)
	{
		service.clear();
	}
}