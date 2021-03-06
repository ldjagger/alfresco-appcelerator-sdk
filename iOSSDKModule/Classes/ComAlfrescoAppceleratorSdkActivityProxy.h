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

//
//  ComAlfrescoAppceleratorSdkActivityProxy.h
//  iOSSDKModule
//
//  Created by Luke Jagger on 10/09/2013.
//
//


#import "TiProxy.h"

/**
#Javascript object:#
<code>Activity</code>
 
#Javascript properties:#
* string identifier
* date createdAt
* string createdBy
* string siteShortName
* string type
* properties data
 
*/

@interface ComAlfrescoAppceleratorSdkActivityProxy : TiProxy

-(id)initWithActivityEntry:(AlfrescoActivityEntry*)entry;

@end
