package org.opencdmp.controllers.swagger;

public final class SwaggerHelpers {

    public static final class Commons {

        public static final String fieldset_description =
                """
                        This is an object containing a list of the properties you wish to include in the response, similar to the 'project' attribute on queries.
                        """;

        public static final String pagination_example =
                """
                        Pagination and projection
                        """;

        public static final String pagination_example_description =
                """
                        Simple paginated request using a property projection list and pagination info
                        """;

        public static final String pagination_response_example =
                """
                        Pagination and projection
                        """;

        public static final String pagination_response_example_description =
                """
                        Simple paginated request using a property projection list and pagination info
                        """;
    }

    public static final class Errors {

        public static final String message_400 =
                """
                        {
                            "code": 400,
                            "message" "validation error",
                            "errors": {
                                "key": "name",
                                "message": [
                                    "name is required"
                                ]
                            }
                        }
                        """;

        public static final String message_403 =
                """
                        {
                            "code": 403,
                            "message": null
                        }
                        """;

        public static final String message_404 =
                """
                        {
                            "code": 404,
                            "message": "Item {0} of type {1} not found"
                        }
                        """;

        public static final String message_500 =
                """
                        {
                            "error": "System error"
                        }
                        """;

    }

    public static final class Plan {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available plans.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Plan specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the description entities that include the contents of the parameter either in their labels or the descriptions will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>groupIds:</b>
                            This is a list and contains the group ids we want the plans to have. Every plan and all its versions, have the same groupId. <br/>If empty, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>statuses:</b>
                            This is a list and determines which records we want to include in the response, based on their status.
                            The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>versionStatuses:</b>
                            This is a list and determines which records we want to include in the response, based on their version status.
                            The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>accessTypes:</b>
                            This is a list and determines which records we want to include in the response, based on their access type.
                            The access type can be <i>Public</i> or <i>Restricted</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "label",
                                 "description",
                                 "status",
                                 "accessType",
                                 "version",
                                 "versionStatus",
                                 "groupId",
                                 "updatedAt",
                                 "belongsToCurrentTenant",
                                 "finalizedAt",
                                 "hash",
                                 "descriptions.id",
                                 "descriptions.label",
                                 "descriptions.status",
                                 "descriptions.descriptionTemplate.groupId",
                                 "descriptions.isActive",
                                 "blueprint.id",
                                 "blueprint.label",
                                 "blueprint.definition.sections.id"
                              ]
                           },
                           "page":{
                              "size":5,
                              "offset":0
                           },
                           "order":{
                              "items":[
                                 "-updatedAt"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "isActive":[
                              1
                           ],
                           "versionStatuses":[
                              0,
                              2
                           ]
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                            {
                               "items":[
                                  {
                                     "id":"dbc5057b-d33e-4a2c-8963-e882da6765a0",
                                     "label":"blueprint test with deleted description template",
                                     "version":1,
                                     "status":0,
                                     "versionStatus":2,
                                     "groupId":"dae2fd0b-74a4-4bde-b021-9ea44e033866",
                                     "description":"blueprint test with deleted description template",
                                     "updatedAt":"2024-06-06T12:35:57.293416Z",
                                     "blueprint":{
                                        "id":"6fbfd40a-b41a-4928-b4b5-67d28bdba2ce",
                                        "label":"test_for_plan_manager_role_new_v2 new ",
                                        "definition":{
                                           "sections":[
                                              {
                                                 "id":"843c4181-dbe2-4e18-a1e4-cf178199947c",
                                                 "label":"1",
                                                 "hasTemplates":true,
                                                 "descriptionTemplates":[
                                                    {
                                                       "descriptionTemplateGroupId":"3689bcce-405a-4d55-9854-669597b79c0a"
                                                    },
                                                    {
                                                       "descriptionTemplateGroupId":"2a38d018-8e81-4ba2-80b2-4395a1d1b238"
                                                    }
                                                 ]
                                              }
                                           ]
                                        }
                                     },
                                     "hash":"1717677357",
                                     "dmpUsers":[
                                        {
                                           "id":"a61fdc31-fb1e-4cee-9c0a-a0a317b27b62",
                                           "dmp":{
                                              "id":"dbc5057b-d33e-4a2c-8963-e882da6765a0"
                                           },
                                           "user":{
                                              "id":"e60876ed-87f8-4a8e-8081-e5620ec839cf"
                                           },
                                           "role":0,
                                           "isActive":1
                                        }
                                     ],
                                     "dmpDescriptionTemplates":[
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"843c4181-dbe2-4e18-a1e4-cf178199947c",
                                           "descriptionTemplateGroupId":"2a38d018-8e81-4ba2-80b2-4395a1d1b238",
                                           "isActive":1
                                        },
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"843c4181-dbe2-4e18-a1e4-cf178199947c",
                                           "descriptionTemplateGroupId":"3689bcce-405a-4d55-9854-669597b79c0a",
                                           "isActive":0
                                        }
                                     ],
                                     "authorizationFlags":[
                                        "InviteDmpUsers",
                                        "CreateNewVersionDmp",
                                        "AssignDmpUsers",
                                        "DeleteDmp",
                                        "FinalizeDmp",
                                        "CloneDmp",
                                        "ExportDmp",
                                        "EditDmp"
                                     ],
                                     "belongsToCurrentTenant":true
                                  },
                                  {
                                     "id":"d3d5cba3-50d0-4dcd-9660-b7bcfac51316",
                                     "label":"description_template_delete_plan_New_v2.xml",
                                     "version":1,
                                     "status":0,
                                     "versionStatus":2,
                                     "groupId":"5ab5d0ca-5c8e-4450-bf3b-a701ceadfabb",
                                     "description":"description template delete plan",
                                     "updatedAt":"2024-06-06T12:30:24.936458Z",
                                     "accessType":0,
                                     "blueprint":{
                                        "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                        "label":"Dmp Default Blueprint",
                                        "definition":{
                                           "sections":[
                                              {
                                                 "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                 "label":"Main Info",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                 "label":"Funding",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                 "label":"License",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                 "label":"Templates",
                                                 "hasTemplates":true
                                              }
                                           ]
                                        }
                                     },
                                     "hash":"1717677024",
                                     "dmpReferences":[
                                        {
                                           "id":"0c470488-14ba-4c7e-8cf2-0f6f849ee56e",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"08577246-b50b-44ea-9bed-641ea4e40fe7",
                                              "label":"unidentified",
                                              "type":{
                                                 "id":"5b9c284f-f041-4995-96cc-fad7ad13289c"
                                              }
                                           },
                                           "isActive":1
                                        },
                                        {
                                           "id":"62211794-787c-407c-9512-05c2d60d410b",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"32c87599-e375-4294-a7cf-3db8722bd675",
                                              "label":"APC Microbiome Institute||",
                                              "type":{
                                                 "id":"538928bb-c7c6-452e-b66d-08e539f5f082"
                                              }
                                           },
                                           "isActive":1
                                        }
                                     ],
                                     "dmpUsers":[
                                        {
                                           "id":"313865d6-6a72-449f-ac04-1834d2f03b02",
                                           "dmp":{
                                              "id":"d3d5cba3-50d0-4dcd-9660-b7bcfac51316"
                                           },
                                           "user":{
                                              "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                           },
                                           "role":0,
                                           "isActive":1
                                        }
                                     ],
                                     "descriptions":[
                                        {
                                           "id":"1dc4e3e8-c125-4db8-a937-041405aaf5ec",
                                           "label":"description template test",
                                           "status":0,
                                           "isActive":0,
                                           "descriptionTemplate":{
                                              "groupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        },
                                        {
                                           "id":"0059fa07-5698-42ba-958a-428b226ddcab",
                                           "label":"test3v2 description template",
                                           "status":0,
                                           "isActive":1,
                                           "descriptionTemplate":{
                                              "groupId":"2a38d018-8e81-4ba2-80b2-4395a1d1b238"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        }
                                     ],
                                     "dmpDescriptionTemplates":[
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                           "descriptionTemplateGroupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e",
                                           "isActive":0
                                        },
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                           "descriptionTemplateGroupId":"2a38d018-8e81-4ba2-80b2-4395a1d1b238",
                                           "isActive":1
                                        }
                                     ],
                                     "authorizationFlags":[
                                        "InviteDmpUsers",
                                        "CreateNewVersionDmp",
                                        "AssignDmpUsers",
                                        "DeleteDmp",
                                        "FinalizeDmp",
                                        "CloneDmp",
                                        "ExportDmp",
                                        "EditDmp"
                                     ],
                                     "belongsToCurrentTenant":true
                                  },
                                  {
                                     "id":"2ebbb8ac-742d-413a-a3e2-1cb21d555b3f",
                                     "label":"description template delete plan New v2",
                                     "version":2,
                                     "status":0,
                                     "versionStatus":2,
                                     "groupId":"cf177996-f68c-4375-bf53-161953dadfc2",
                                     "description":"description template delete plan",
                                     "updatedAt":"2024-06-06T11:59:21.396453Z",
                                     "accessType":0,
                                     "blueprint":{
                                        "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                        "label":"Dmp Default Blueprint",
                                        "definition":{
                                           "sections":[
                                              {
                                                 "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                 "label":"Main Info",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                 "label":"Funding",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                 "label":"License",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                 "label":"Templates",
                                                 "hasTemplates":true
                                              }
                                           ]
                                        }
                                     },
                                     "hash":"1717675161",
                                     "dmpReferences":[
                                        {
                                           "id":"5cd1a1c3-bd9b-4a89-9329-ab3f152eebc7",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"32c87599-e375-4294-a7cf-3db8722bd675",
                                              "label":"APC Microbiome Institute||",
                                              "type":{
                                                 "id":"538928bb-c7c6-452e-b66d-08e539f5f082"
                                              }
                                           },
                                           "isActive":1
                                        },
                                        {
                                           "id":"0e5d165f-b8ae-44a2-8d90-5e8e1037caa7",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"08577246-b50b-44ea-9bed-641ea4e40fe7",
                                              "label":"unidentified",
                                              "type":{
                                                 "id":"5b9c284f-f041-4995-96cc-fad7ad13289c"
                                              }
                                           },
                                           "isActive":1
                                        }
                                     ],
                                     "dmpUsers":[
                                        {
                                           "id":"83b1680d-3d9a-4610-82d5-8fe0132f2a0b",
                                           "dmp":{
                                              "id":"2ebbb8ac-742d-413a-a3e2-1cb21d555b3f"
                                           },
                                           "user":{
                                              "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                           },
                                           "role":0,
                                           "isActive":1
                                        }
                                     ],
                                     "descriptions":[
                                        {
                                           "id":"2620507c-cb8b-4d67-bb42-64a702932b0a",
                                           "label":"description template test",
                                           "status":0,
                                           "isActive":1,
                                           "descriptionTemplate":{
                                              "groupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        }
                                     ],
                                     "dmpDescriptionTemplates":[
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                           "descriptionTemplateGroupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e",
                                           "isActive":1
                                        }
                                     ],
                                     "authorizationFlags":[
                                        "InviteDmpUsers",
                                        "CreateNewVersionDmp",
                                        "AssignDmpUsers",
                                        "DeleteDmp",
                                        "FinalizeDmp",
                                        "CloneDmp",
                                        "ExportDmp",
                                        "EditDmp"
                                     ],
                                     "belongsToCurrentTenant":true
                                  },
                                  {
                                     "id":"68e9aae2-f2f9-4d51-b5f4-9a0216efb00c",
                                     "label":"description template delete plan New",
                                     "version":1,
                                     "status":1,
                                     "versionStatus":0,
                                     "groupId":"cf177996-f68c-4375-bf53-161953dadfc2",
                                     "description":"description template delete plan",
                                     "updatedAt":"2024-06-06T11:27:32.988143Z",
                                     "finalizedAt":"2024-06-06T11:27:32.988143Z",
                                     "accessType":0,
                                     "blueprint":{
                                        "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                        "label":"Dmp Default Blueprint",
                                        "definition":{
                                           "sections":[
                                              {
                                                 "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                 "label":"Main Info",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                 "label":"Funding",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                 "label":"License",
                                                 "hasTemplates":false
                                              },
                                              {
                                                 "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                 "label":"Templates",
                                                 "hasTemplates":true
                                              }
                                           ]
                                        }
                                     },
                                     "hash":"1717673252",
                                     "dmpReferences":[
                                        {
                                           "id":"c1510688-4a70-4a70-abc0-9778d5db5d7f",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"32c87599-e375-4294-a7cf-3db8722bd675",
                                              "label":"APC Microbiome Institute||",
                                              "type":{
                                                 "id":"538928bb-c7c6-452e-b66d-08e539f5f082"
                                              }
                                           },
                                           "isActive":1
                                        },
                                        {
                                           "id":"f9755a49-c305-4afe-889f-3ec1834b139a",
                                           "dmp":{
                                             \s
                                           },
                                           "reference":{
                                              "id":"08577246-b50b-44ea-9bed-641ea4e40fe7",
                                              "label":"unidentified",
                                              "type":{
                                                 "id":"5b9c284f-f041-4995-96cc-fad7ad13289c"
                                              }
                                           },
                                           "isActive":1
                                        }
                                     ],
                                     "dmpUsers":[
                                        {
                                           "id":"dd48a20d-5166-453b-9a25-bc5ec49ade1f",
                                           "dmp":{
                                              "id":"68e9aae2-f2f9-4d51-b5f4-9a0216efb00c"
                                           },
                                           "user":{
                                              "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                           },
                                           "role":0,
                                           "isActive":1
                                        }
                                     ],
                                     "descriptions":[
                                        {
                                           "id":"18820f0b-8222-49fa-a997-86a171c41092",
                                           "label":"description template test",
                                           "status":1,
                                           "isActive":1,
                                           "descriptionTemplate":{
                                              "groupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        }
                                     ],
                                     "dmpDescriptionTemplates":[
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                           "descriptionTemplateGroupId":"fab932b2-4285-44a2-b9ab-1a4306bd3e0e",
                                           "isActive":1
                                        }
                                     ],
                                     "authorizationFlags":[
                                        "InviteDmpUsers",
                                        "CreateNewVersionDmp",
                                        "AssignDmpUsers",
                                        "DeleteDmp",
                                        "FinalizeDmp",
                                        "CloneDmp",
                                        "ExportDmp",
                                        "EditDmp"
                                     ],
                                     "belongsToCurrentTenant":true
                                  },
                                  {
                                     "id":"f2e807f1-7fd4-48db-b781-e9c972057447",
                                     "label":"user3 plan",
                                     "version":1,
                                     "status":0,
                                     "versionStatus":2,
                                     "groupId":"df98fefc-6213-450d-aea7-594b43421c3f",
                                     "description":"test",
                                     "updatedAt":"2024-06-06T11:03:56.668426Z",
                                     "blueprint":{
                                        "id":"fc83c102-8c3c-4298-8e64-02bdd0fb7275",
                                        "label":"blueprint with users",
                                        "definition":{
                                           "sections":[
                                              {
                                                 "id":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                                 "label":"Main info",
                                                 "hasTemplates":true,
                                                 "descriptionTemplates":[
                                                    {
                                                       "descriptionTemplateGroupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                                    }
                                                 ]
                                              },
                                              {
                                                 "id":"109ceb1e-9703-2a03-462b-8bf93f993a53",
                                                 "label":"Section2",
                                                 "hasTemplates":true
                                              }
                                           ]
                                        }
                                     },
                                     "hash":"1717671836",
                                     "dmpUsers":[
                                        {
                                           "id":"fcce59eb-8e9e-4768-92fe-e772886836f2",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                           },
                                           "role":0,
                                           "isActive":1
                                        },
                                        {
                                           "id":"8f698ad7-69e4-4fcf-b9b3-563b1b05ac45",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":3,
                                           "isActive":0
                                        },
                                        {
                                           "id":"0b26b2eb-5346-463c-b0f4-fa22d9a4c964",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":3,
                                           "isActive":0
                                        },
                                        {
                                           "id":"5154ec6e-912d-4b4f-8f1e-cc2a34069344",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":1,
                                           "isActive":0
                                        },
                                        {
                                           "id":"9ec5f356-43a3-462d-bbf2-27476931892f",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":1,
                                           "isActive":0
                                        },
                                        {
                                           "id":"e4e6a68c-408e-47fa-8b40-cb8c155f9eda",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":2,
                                           "isActive":0
                                        },
                                        {
                                           "id":"f1de4a02-137f-48d9-bd31-5904b2ba504a",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":3,
                                           "isActive":0
                                        },
                                        {
                                           "id":"9e444526-94c4-4e50-9c35-adf73fab5f8c",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":3,
                                           "isActive":0
                                        },
                                        {
                                           "id":"c92686d2-b434-44e3-b8fb-ef0d6a30582d",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":3,
                                           "isActive":1
                                        },
                                        {
                                           "id":"42f9c224-8704-4c41-8a2d-71b5c78d7690",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":2,
                                           "isActive":1
                                        },
                                        {
                                           "id":"be5836f4-842a-4a74-b852-c4bb4212adb0",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":2,
                                           "isActive":0
                                        },
                                        {
                                           "id":"18bb660a-638c-4def-90da-0c83368f7c1e",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":2,
                                           "isActive":1
                                        },
                                        {
                                           "id":"e5a8f9d1-30a3-4ee0-8ca6-f74c5e73ba53",
                                           "dmp":{
                                              "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                           },
                                           "user":{
                                              "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                           },
                                           "role":2,
                                           "isActive":1
                                        }
                                     ],
                                     "descriptions":[
                                        {
                                           "id":"49b4c2df-0bad-4173-85d4-c5b416a7125a",
                                           "label":"user3 description",
                                           "status":0,
                                           "isActive":1,
                                           "descriptionTemplate":{
                                              "groupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        },
                                        {
                                           "id":"546a6195-17b6-4d1e-bad2-fa322fbad3f1",
                                           "label":"user3 description - to delete",
                                           "status":0,
                                           "isActive":1,
                                           "descriptionTemplate":{
                                              "groupId":"33dc4612-32bf-46a3-b040-12e32a06b3c7"
                                           },
                                           "dmp":{
                                             \s
                                           }
                                        }
                                     ],
                                     "dmpDescriptionTemplates":[
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                           "descriptionTemplateGroupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12",
                                           "isActive":1
                                        },
                                        {
                                           "dmp":{
                                             \s
                                           },
                                           "sectionId":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                           "descriptionTemplateGroupId":"33dc4612-32bf-46a3-b040-12e32a06b3c7",
                                           "isActive":0
                                        }
                                     ],
                                     "authorizationFlags":[
                                        "InviteDmpUsers",
                                        "CreateNewVersionDmp",
                                        "AssignDmpUsers",
                                        "DeleteDmp",
                                        "FinalizeDmp",
                                        "CloneDmp",
                                        "ExportDmp",
                                        "EditDmp"
                                     ],
                                     "belongsToCurrentTenant":true
                                  }
                               ],
                               "count":2912
                            }
                        """;
    }

    public static final class Description {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available descriptions.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Description specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the description entities that include the contents of the parameter either in their labels or the descriptions will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>statuses:</b>
                            This is a list and determines which records we want to include in the response, based on their status.
                            The status can be <i>Draft</i>, <i>Finalized</i> or <i>Canceled</i>. We add 0, 1 or 2 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>createdAfter:</b>
                            This is a date and determines which records we want to include in the response, based on their creation date.
                            Specifically, only the records created after the given date are included.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>createdBefore:</b>
                            This is a date and determines which records we want to include in the response, based on their creation date.
                            Specifically, only the records created before the given date are included.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>finalizedAfter:</b>
                            This is a date and determines which records we want to include in the response, based on their finalization date.
                            Specifically, only the records finalized after the given date are included.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>finalizedBefore:</b>
                            This is a date and determines which records we want to include in the response, based on their finalization date.
                            Specifically, only the records finalized before the given date are included.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "label",
                                 "status",
                                 "updatedAt",
                                 "belongsToCurrentTenant",
                                 "finalizedAt",
                                 "descriptionTemplate.id",
                                 "descriptionTemplate.label",
                                 "descriptionTemplate.groupId",
                                 "dmp.id",
                                 "dmp.label",
                                 "dmp.status",
                                 "dmp.accessType",
                                 "dmp.blueprint.id",
                                 "dmp.blueprint.label",
                                 "dmp.blueprint.definition.sections.id",
                                 "dmp.blueprint.definition.sections.label",
                                 "dmp.blueprint.definition.sections.hasTemplates",
                                 "dmp.dmpReferences.id",
                                 "dmp.dmpReferences.reference.id",
                                 "dmp.dmpReferences.reference.label",
                                 "dmp.dmpReferences.reference.type.id",
                                 "dmp.dmpReferences.reference.reference",
                                 "dmp.dmpReferences.isActive",
                                 "dmpDescriptionTemplate.id",
                                 "dmpDescriptionTemplate.dmp.id",
                                 "dmpDescriptionTemplate.descriptionTemplateGroupId",
                                 "dmpDescriptionTemplate.sectionId"
                              ]
                           },
                           "page":{
                              "size":5,
                              "offset":0
                           },
                           "order":{
                              "items":[
                                 "-updatedAt"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "isActive":[
                              1
                           ]
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"a18258c6-8a47-4716-81f3-9cbe383af182",
                                 "label":"delete template",
                                 "status":0,
                                 "updatedAt":"2024-06-06T13:15:15.763826Z",
                                 "dmpDescriptionTemplate":{
                                    "id":"29a3385c-a2ae-4675-b1c6-0fd5a49017be",
                                    "dmp":{
                                       "id":"9dcb614e-05c5-4679-bbe8-7cb8913586e8"
                                    },
                                    "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                    "descriptionTemplateGroupId":"f0979866-f9d1-4eb3-b33e-572e744ce8af"
                                 },
                                 "descriptionTemplate":{
                                    "id":"2ea4c926-df57-48fa-b22d-2b2bafae1267",
                                    "label":"delete",
                                    "groupId":"f0979866-f9d1-4eb3-b33e-572e744ce8af"
                                 },
                                 "authorizationFlags":[
                                    "DeleteDescription",
                                    "EditDescription",
                                    "InviteDmpUsers"
                                 ],
                                 "dmp":{
                                    "id":"9dcb614e-05c5-4679-bbe8-7cb8913586e8",
                                    "label":"description template delete plan",
                                    "status":0,
                                    "blueprint":{
                                       "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                       "label":"Dmp Default Blueprint",
                                       "definition":{
                                          "sections":[
                                             {
                                                "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                "label":"Main Info",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                "label":"Funding",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                "label":"License",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                "label":"Templates",
                                                "hasTemplates":true
                                             }
                                          ]
                                       }
                                    },
                                    "dmpUsers":[
                                       {
                                          "id":"cbd64e90-846e-4c4e-8678-89ebc54b1c5f",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"e60876ed-87f8-4a8e-8081-e5620ec839cf"
                                          },
                                          "role":0,
                                          "isActive":1
                                       }
                                    ]
                                 },
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"46c9be1b-9809-4bd3-b730-ce4a9b457484",
                                 "label":"description from default",
                                 "status":0,
                                 "updatedAt":"2024-06-06T12:56:33.171675Z",
                                 "dmpDescriptionTemplate":{
                                    "id":"69ea7b42-e5e4-4b23-b17d-9883ece3ee53",
                                    "dmp":{
                                       "id":"703b00d0-af49-4e5a-9002-fc3f4ae6e107"
                                    },
                                    "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                    "descriptionTemplateGroupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "descriptionTemplate":{
                                    "id":"57ced1f1-6e8c-482a-88c4-2e2d6322b25c",
                                    "label":"Academy Of Finland",
                                    "groupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "authorizationFlags":[
                                    "DeleteDescription",
                                    "EditDescription",
                                    "InviteDmpUsers"
                                 ],
                                 "dmp":{
                                    "id":"703b00d0-af49-4e5a-9002-fc3f4ae6e107",
                                    "label":"plan from default",
                                    "status":0,
                                    "blueprint":{
                                       "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                       "label":"Dmp Default Blueprint",
                                       "definition":{
                                          "sections":[
                                             {
                                                "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                "label":"Main Info",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                "label":"Funding",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                "label":"License",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                "label":"Templates",
                                                "hasTemplates":true
                                             }
                                          ]
                                       }
                                    },
                                    "dmpUsers":[
                                       {
                                          "id":"dff384f8-3e06-47b9-84f9-b91b66ab3327",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"e60876ed-87f8-4a8e-8081-e5620ec839cf"
                                          },
                                          "role":0,
                                          "isActive":1
                                       }
                                    ]
                                 },
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"546a6195-17b6-4d1e-bad2-fa322fbad3f1",
                                 "label":"user3 description - to delete",
                                 "status":0,
                                 "updatedAt":"2024-06-06T09:58:17.507386Z",
                                 "dmpDescriptionTemplate":{
                                    "id":"0e8a5e65-c3bc-49b0-a6f3-d7da22836f3b",
                                    "dmp":{
                                       "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                    },
                                    "sectionId":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                    "descriptionTemplateGroupId":"33dc4612-32bf-46a3-b040-12e32a06b3c7"
                                 },
                                 "descriptionTemplate":{
                                    "id":"d8a1ac10-ffa4-4f7f-afe9-36b904af4ae7",
                                    "label":"RDA_madmp-only new TO DELETE ",
                                    "groupId":"33dc4612-32bf-46a3-b040-12e32a06b3c7"
                                 },
                                 "authorizationFlags":[
                                    "DeleteDescription",
                                    "EditDescription",
                                    "InviteDmpUsers"
                                 ],
                                 "dmp":{
                                    "id":"f2e807f1-7fd4-48db-b781-e9c972057447",
                                    "label":"user3 plan",
                                    "status":0,
                                    "blueprint":{
                                       "id":"fc83c102-8c3c-4298-8e64-02bdd0fb7275",
                                       "label":"blueprint with users",
                                       "definition":{
                                          "sections":[
                                             {
                                                "id":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                                "label":"Main info",
                                                "hasTemplates":true
                                             },
                                             {
                                                "id":"109ceb1e-9703-2a03-462b-8bf93f993a53",
                                                "label":"Section2",
                                                "hasTemplates":true
                                             }
                                          ]
                                       }
                                    },
                                    "dmpUsers":[
                                       {
                                          "id":"fcce59eb-8e9e-4768-92fe-e772886836f2",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                          },
                                          "role":0,
                                          "isActive":1
                                       },
                                       {
                                          "id":"8f698ad7-69e4-4fcf-b9b3-563b1b05ac45",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"0b26b2eb-5346-463c-b0f4-fa22d9a4c964",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"5154ec6e-912d-4b4f-8f1e-cc2a34069344",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":1,
                                          "isActive":0
                                       },
                                       {
                                          "id":"9ec5f356-43a3-462d-bbf2-27476931892f",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":1,
                                          "isActive":0
                                       },
                                       {
                                          "id":"e4e6a68c-408e-47fa-8b40-cb8c155f9eda",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":0
                                       },
                                       {
                                          "id":"f1de4a02-137f-48d9-bd31-5904b2ba504a",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"9e444526-94c4-4e50-9c35-adf73fab5f8c",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"c92686d2-b434-44e3-b8fb-ef0d6a30582d",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":1
                                       },
                                       {
                                          "id":"42f9c224-8704-4c41-8a2d-71b5c78d7690",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       },
                                       {
                                          "id":"be5836f4-842a-4a74-b852-c4bb4212adb0",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":0
                                       },
                                       {
                                          "id":"18bb660a-638c-4def-90da-0c83368f7c1e",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       },
                                       {
                                          "id":"e5a8f9d1-30a3-4ee0-8ca6-f74c5e73ba53",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       }
                                    ]
                                 },
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"49b4c2df-0bad-4173-85d4-c5b416a7125a",
                                 "label":"user3 description",
                                 "status":0,
                                 "updatedAt":"2024-06-05T14:43:20.353594Z",
                                 "dmpDescriptionTemplate":{
                                    "id":"3432e0b5-fbda-4a81-ab56-06657636da57",
                                    "dmp":{
                                       "id":"f2e807f1-7fd4-48db-b781-e9c972057447"
                                    },
                                    "sectionId":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                    "descriptionTemplateGroupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "descriptionTemplate":{
                                    "id":"57ced1f1-6e8c-482a-88c4-2e2d6322b25c",
                                    "label":"Academy Of Finland",
                                    "groupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "authorizationFlags":[
                                    "DeleteDescription",
                                    "EditDescription",
                                    "InviteDmpUsers"
                                 ],
                                 "dmp":{
                                    "id":"f2e807f1-7fd4-48db-b781-e9c972057447",
                                    "label":"user3 plan",
                                    "status":0,
                                    "blueprint":{
                                       "id":"fc83c102-8c3c-4298-8e64-02bdd0fb7275",
                                       "label":"blueprint with users",
                                       "definition":{
                                          "sections":[
                                             {
                                                "id":"44e6e93b-a6e0-6036-bda5-b33fe1df2f02",
                                                "label":"Main info",
                                                "hasTemplates":true
                                             },
                                             {
                                                "id":"109ceb1e-9703-2a03-462b-8bf93f993a53",
                                                "label":"Section2",
                                                "hasTemplates":true
                                             }
                                          ]
                                       }
                                    },
                                    "dmpUsers":[
                                       {
                                          "id":"fcce59eb-8e9e-4768-92fe-e772886836f2",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                          },
                                          "role":0,
                                          "isActive":1
                                       },
                                       {
                                          "id":"8f698ad7-69e4-4fcf-b9b3-563b1b05ac45",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"0b26b2eb-5346-463c-b0f4-fa22d9a4c964",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"5154ec6e-912d-4b4f-8f1e-cc2a34069344",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":1,
                                          "isActive":0
                                       },
                                       {
                                          "id":"9ec5f356-43a3-462d-bbf2-27476931892f",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":1,
                                          "isActive":0
                                       },
                                       {
                                          "id":"e4e6a68c-408e-47fa-8b40-cb8c155f9eda",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":0
                                       },
                                       {
                                          "id":"f1de4a02-137f-48d9-bd31-5904b2ba504a",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"9e444526-94c4-4e50-9c35-adf73fab5f8c",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":0
                                       },
                                       {
                                          "id":"c92686d2-b434-44e3-b8fb-ef0d6a30582d",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":3,
                                          "isActive":1
                                       },
                                       {
                                          "id":"42f9c224-8704-4c41-8a2d-71b5c78d7690",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       },
                                       {
                                          "id":"be5836f4-842a-4a74-b852-c4bb4212adb0",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":0
                                       },
                                       {
                                          "id":"18bb660a-638c-4def-90da-0c83368f7c1e",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       },
                                       {
                                          "id":"e5a8f9d1-30a3-4ee0-8ca6-f74c5e73ba53",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":2,
                                          "isActive":1
                                       }
                                    ]
                                 },
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"9ac982e6-1cdd-4e03-b083-e91911114cc7",
                                 "label":"test",
                                 "status":0,
                                 "updatedAt":"2024-06-05T13:54:15.215978Z",
                                 "dmpDescriptionTemplate":{
                                    "id":"af011eaf-211c-40f5-af02-66a86912349a",
                                    "dmp":{
                                       "id":"c5c9b67f-3c27-4677-900a-1f6f799f2bc9"
                                    },
                                    "sectionId":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                    "descriptionTemplateGroupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "descriptionTemplate":{
                                    "id":"57ced1f1-6e8c-482a-88c4-2e2d6322b25c",
                                    "label":"Academy Of Finland",
                                    "groupId":"11559a4f-d6f4-4dc9-8e6b-d1879700ac12"
                                 },
                                 "authorizationFlags":[
                                    "DeleteDescription",
                                    "EditDescription",
                                    "InviteDmpUsers"
                                 ],
                                 "dmp":{
                                    "id":"c5c9b67f-3c27-4677-900a-1f6f799f2bc9",
                                    "label":"test export contact",
                                    "status":0,
                                    "accessType":1,
                                    "blueprint":{
                                       "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                       "label":"Dmp Default Blueprint",
                                       "definition":{
                                          "sections":[
                                             {
                                                "id":"f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                "label":"Main Info",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                "label":"Funding",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                "label":"License",
                                                "hasTemplates":false
                                             },
                                             {
                                                "id":"0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                "label":"Templates",
                                                "hasTemplates":true
                                             }
                                          ]
                                       }
                                    },
                                    "dmpReferences":[
                                       {
                                          "id":"f74eca5b-f865-440d-b888-7ebe8482290a",
                                          "dmp":{
                                            \s
                                          },
                                          "reference":{
                                             "id":"ab7a87f2-69bb-4b14-94fc-6b65d822a8b2",
                                             "label":"Archiv der Deutschen Frauenbewegung",
                                             "type":{
                                                "id":"7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                             },
                                             "reference":"pending_org_::44714f780f925d981f99b085346d1e2a"
                                          },
                                          "isActive":1
                                       },
                                       {
                                          "id":"09e66ee1-4b29-4195-9a9b-f931af1b093f",
                                          "dmp":{
                                            \s
                                          },
                                          "reference":{
                                             "id":"8ea5ff4d-4a0a-4baa-8dc1-7f1de31bc3e2",
                                             "label":"Aisha Mohammed (0000-0002-0131-1543)",
                                             "type":{
                                                "id":"5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                             },
                                             "reference":"0000-0002-0131-1543"
                                          },
                                          "isActive":1
                                       },
                                       {
                                          "id":"ffdfec53-2a53-4953-a977-76149bc269af",
                                          "dmp":{
                                            \s
                                          },
                                          "reference":{
                                             "id":"7aed6061-39a9-4a86-944b-f7c49024ccfc",
                                             "label":"Autonomously Assembling Nanomaterial Scaffolds for Treating Myocardial Infarction (nih_________::5R01HL117326-03)",
                                             "type":{
                                                "id":"5b9c284f-f041-4995-96cc-fad7ad13289c"
                                             },
                                             "reference":"nih_________::5R01HL117326-03"
                                          },
                                          "isActive":1
                                       },
                                       {
                                          "id":"48877b47-fd2b-42f7-b7f0-bc6a379f0971",
                                          "dmp":{
                                            \s
                                          },
                                          "reference":{
                                             "id":"98435709-ccc6-42c7-aa96-30b7e4d8f317",
                                             "label":"ADAPT - Centre for Digital Content Technology||",
                                             "type":{
                                                "id":"538928bb-c7c6-452e-b66d-08e539f5f082"
                                             },
                                             "reference":"501100014826::501100014826||ADAPT - Centre for Digital Content Technology||"
                                          },
                                          "isActive":1
                                       }
                                    ],
                                    "dmpUsers":[
                                       {
                                          "id":"bcf92d98-1492-4423-b1a2-2b1ef76ea25e",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"391252ba-926d-4bf9-8c28-a921036f6c39"
                                          },
                                          "role":0,
                                          "isActive":1
                                       },
                                       {
                                          "id":"8caa96de-82f7-4593-b0f6-7e88cfe76530",
                                          "dmp":{
                                            \s
                                          },
                                          "user":{
                                             "id":"7d6e54b6-31e8-48df-9c3f-1e5b3eb48404"
                                          },
                                          "role":0,
                                          "isActive":1
                                       }
                                    ]
                                 },
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":4006
                        }
                        """;
    }

    public static final class DescriptionTemplate {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available description templates.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                       
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Description template specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the description template entities that include the contents of the parameter either in their labels or the descriptions will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>groupIds:</b>
                            This is a list and contains the group ids we want the templates to have. Every template and all its versions, have the same groupId. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedGroupIds:</b>
                            This is a list and contains the group ids we want the templates not to have. Every template and all its versions, have the same groupId. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>typeIds:</b>
                            This is a list and contains the type ids we want the templates to have. Every template has a type designated by a type id. <br/>If empty, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>statuses:</b>
                            This is a list and determines which records we want to include in the response, based on their status.
                            The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>versionStatuses:</b>
                            This is a list and determines which records we want to include in the response, based on their version status.
                            The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>onlyCanEdit:</b>
                            This is a boolean and determines whether to fetch only the templates the user can edit or every one.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                            "project":{
                               "fields":[
                                  "id",
                                  "label",
                                  "description",
                                  "status",
                                  "version",
                                  "groupId",
                                  "updatedAt",
                                  "createdAt",
                                  "hash",
                                  "belongsToCurrentTenant",
                                  "isActive",
                                  "authorizationFlags.EditDescriptionTemplate",
                                  "authorizationFlags.DeleteDescriptionTemplate",
                                  "authorizationFlags.CloneDescriptionTemplate",
                                  "authorizationFlags.CreateNewVersionDescriptionTemplate",
                                  "authorizationFlags.ImportDescriptionTemplate",
                                  "authorizationFlags.ExportDescriptionTemplate"
                               ]
                            },
                            "metadata":{
                               "countAll":true
                            },
                            "page":{
                               "offset":0,
                               "size":10
                            },
                            "isActive":[
                               1
                            ],
                            "order":{
                               "items":[
                                  "-createdAt"
                               ]
                            },
                            "versionStatuses":[
                               0,
                               2
                            ],
                            "onlyCanEdit":true
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"fbbea99b-9259-430b-9f1b-c78f3df8a07c",
                                 "label":"url",
                                 "description":"zxccx",
                                 "groupId":"71d1b878-9980-4b35-b58d-98597e1f2579",
                                 "version":1,
                                 "createdAt":"2024-06-21T13:13:38.344835Z",
                                 "updatedAt":"2024-06-21T13:13:43.001378Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718975623",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"6d2c5983-6125-4fc0-833a-3618d35d0c72",
                                 "label":"to-delete",
                                 "description":"test",
                                 "groupId":"95fc6c79-ee17-4cd9-bad7-0d70ce17b9a4",
                                 "version":1,
                                 "createdAt":"2024-06-21T10:22:33.116729Z",
                                 "updatedAt":"2024-06-21T10:22:52.668175Z",
                                 "isActive":1,
                                 "status":0,
                                 "hash":"1718965372",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"e0cb75fe-6f3b-4770-8857-f6b3de9d5411",
                                 "label":"Test Purple Roles",
                                 "description":"Test Purple Roles",
                                 "groupId":"bd3159c7-0b42-431c-8c0f-b353c1343b79",
                                 "version":1,
                                 "createdAt":"2024-06-19T09:53:10.567662Z",
                                 "updatedAt":"2024-06-19T09:53:18.122353Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718790798",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"17eeb5ef-0f2a-40bb-b9b3-4e89a749a08a",
                                 "label":"Purple_template_1.xml",
                                 "description":"Desc",
                                 "groupId":"419bbe42-91e0-4ba4-97cf-c9502063a7ef",
                                 "version":1,
                                 "createdAt":"2024-06-19T09:30:34.925496Z",
                                 "updatedAt":"2024-06-19T09:31:05.429197Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718789465",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"9f6bd3bc-b611-4641-bceb-2e64b0bd34cd",
                                 "label":"RDA_test_new_.xml",
                                 "description":"RDA test",
                                 "groupId":"3c88259f-4b16-4fc3-adf2-cee2ea4b882d",
                                 "version":2,
                                 "createdAt":"2024-06-19T07:14:11.748649Z",
                                 "updatedAt":"2024-06-19T07:14:11.748649Z",
                                 "isActive":1,
                                 "status":0,
                                 "hash":"1718781251",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"212342fe-ab6f-4604-b80e-ac23aca93c76",
                                 "label":"RDA test with fix multiplicity1",
                                 "description":"RDA test",
                                 "groupId":"4ff3e924-8623-4b58-a61b-94740b67c4aa",
                                 "version":1,
                                 "createdAt":"2024-06-18T14:04:55.574099Z",
                                 "updatedAt":"2024-06-20T10:45:35.188002Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718880335",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"dddf404e-da59-4097-86d9-29f085ee40da",
                                 "label":"RDA test new ",
                                 "description":"RDA test",
                                 "groupId":"3c88259f-4b16-4fc3-adf2-cee2ea4b882d",
                                 "version":1,
                                 "createdAt":"2024-06-17T12:09:24.296094Z",
                                 "updatedAt":"2024-06-17T12:20:40.432586Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718626840",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"fa67ecbf-c3b4-4e19-b531-764cf7b4d508",
                                 "label":"test",
                                 "description":"test",
                                 "groupId":"bd1a655e-a47b-4edc-a9a2-214dc2971c12",
                                 "version":1,
                                 "createdAt":"2024-06-17T08:41:36.664047Z",
                                 "updatedAt":"2024-06-17T08:41:36.678049Z",
                                 "isActive":1,
                                 "status":0,
                                 "hash":"1718613696",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"54527748-7c8a-4045-8b8e-f7be65900302",
                                 "label":"RDA test",
                                 "description":"RDA test",
                                 "groupId":"bf36f613-422a-4d4a-aee1-8a377157f265",
                                 "version":1,
                                 "createdAt":"2024-06-14T08:10:55.988008Z",
                                 "updatedAt":"2024-06-14T10:42:17.438789Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718361737",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"d2f41a59-d369-431d-bf3a-eb29fb4423e7",
                                 "label":"CHIST-ERA Data Management",
                                 "description":"This is a DMP template created based on the CHIST-ERA RDM policy and DMP template.",
                                 "groupId":"c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a",
                                 "version":3,
                                 "createdAt":"2024-04-17T16:58:57Z",
                                 "updatedAt":"2024-04-17T16:58:57Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1713373137",
                                 "authorizationFlags":[
                                    "CreateNewVersionDescriptionTemplate",
                                    "CloneDescriptionTemplate",
                                    "ImportDescriptionTemplate",
                                    "DeleteDescriptionTemplate",
                                    "EditDescriptionTemplate",
                                    "ExportDescriptionTemplate"
                                 ],
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":40
                        }
                        """;
    }

    public static final class DescriptionTemplateType {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available description template types.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Description template type specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the description template type entities that include the contents of the parameter in their labels will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>isActives:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>statuses:</b>
                            This is a list and determines which records we want to include in the response, based on their status.
                            The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "name",
                                 "status",
                                 "updatedAt",
                                 "createdAt",
                                 "hash",
                                 "belongsToCurrentTenant",
                                 "isActive"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"709a8400-10ca-11ee-be56-0242ac120002",
                                 "name":"Dataset",
                                 "createdAt":"2024-06-10T07:54:59.508261Z",
                                 "updatedAt":"2024-06-10T07:54:59.508261Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718006099",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"3b15c046-a978-4b5a-9376-66525b7f1ac9",
                                 "name":"Software",
                                 "createdAt":"2024-06-10T07:54:59.508261Z",
                                 "updatedAt":"2024-06-10T07:54:59.508261Z",
                                 "isActive":1,
                                 "status":1,
                                 "hash":"1718006099",
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":2
                        }
                        """;

    }

    public static final class PlanBlueprint {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available blueprints for the plans.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Plan blueprint specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the blueprint entities that include the contents of the parameter in their labels will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>groupIds:</b>
                            This is a list and contains the group ids we want the blueprints to have. Every blueprint and all its versions, have the same groupId. <br/>If empty, every record is included.
                            </li>
                            <li><b>isActives:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>statuses:</b>
                            This is a list and determines which records we want to include in the response, based on their status.
                            The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>versionStatuses:</b>
                            This is a list and determines which records we want to include in the response, based on their version status.
                            The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "label",
                                 "status",
                                 "version",
                                 "groupId",
                                 "updatedAt",
                                 "createdAt",
                                 "hash",
                                 "isActive",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           },
                           "versionStatuses":[
                              0,
                              2
                           ]
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"ef8d828b-a127-4621-8a75-c607ddc441b8",
                                 "label":"Purple blueprint cl.1v.2",
                                 "status":1,
                                 "groupId":"60b43495-4c45-42c6-a9e0-462180b968a5",
                                 "version":2,
                                 "createdAt":"2024-06-19T09:54:19.227934Z",
                                 "updatedAt":"2024-06-19T09:54:25.208065Z",
                                 "isActive":1,
                                 "hash":"1718790865",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"ff3d5601-45b1-4211-ab4b-012c344827d7",
                                 "label":"Latvian_Council_of_Science_Blueprint.xml",
                                 "status":0,
                                 "groupId":"63b61613-d864-4f49-9d5b-76b5273d83dc",
                                 "version":2,
                                 "createdAt":"2024-06-19T07:14:37.558442Z",
                                 "updatedAt":"2024-06-19T07:14:37.558442Z",
                                 "isActive":1,
                                 "hash":"1718781277",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"4c64705f-ff8b-4135-8dd7-4ad147fb46b2",
                                 "label":"RDA Blueprint",
                                 "status":1,
                                 "groupId":"3c3589e5-5ce0-405e-a754-db9e2c9af371",
                                 "version":1,
                                 "createdAt":"2024-06-14T09:15:27.082540Z",
                                 "updatedAt":"2024-06-14T10:42:50.075378Z",
                                 "isActive":1,
                                 "hash":"1718361770",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"bcbbdc41-dbb2-444f-86d3-dd33eda8e0a8",
                                 "label":"University of Bologna",
                                 "status":1,
                                 "groupId":"595efece-365a-43bd-99db-cfa9141bcf94",
                                 "version":1,
                                 "createdAt":"2024-04-11T09:14:09Z",
                                 "updatedAt":"2024-04-11T09:14:40Z",
                                 "isActive":1,
                                 "hash":"1712826880",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"eb02f2ae-69fd-456d-8143-6b0a98af64c4",
                                 "label":"Latvian Council of Science Blueprint",
                                 "status":1,
                                 "groupId":"63b61613-d864-4f49-9d5b-76b5273d83dc",
                                 "version":1,
                                 "createdAt":"2024-03-25T12:33:59Z",
                                 "updatedAt":"2024-03-04T10:09:05Z",
                                 "isActive":1,
                                 "hash":"1709546945",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"86635178-36a6-484f-9057-a934e4eeecd5",
                                 "label":"Dmp Default Blueprint",
                                 "status":1,
                                 "groupId":"20da24e2-575d-40e5-aea4-0986c982fd9c",
                                 "version":1,
                                 "createdAt":"2024-02-15T10:26:49.804431Z",
                                 "updatedAt":"2024-02-15T10:26:49.804431Z",
                                 "isActive":1,
                                 "hash":"1707992809",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"37701076-e0ff-4e4f-95aa-9f3d6a23083a",
                                 "label":"CHIST-ERA",
                                 "status":1,
                                 "groupId":"b7eb5ba1-4cc6-4f74-9eb6-b47a6153cba5",
                                 "version":1,
                                 "createdAt":"2023-10-03T09:00:48Z",
                                 "updatedAt":"2023-10-03T09:01:00Z",
                                 "isActive":1,
                                 "hash":"1696323660",
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":7
                        }
                        """;

    }

    public static final class FileTransformer {

        public static final String endpoint_get_available_transformers =
                """
                        This endpoint is used to fetch all the available file transformers.</br>
                        """;

        public static final String endpoint_export_plans =
                """
                        This endpoint is used to export a plan using a specific file transformer.</br>
                        Choosing the transformer will determine the format of the export.
                        """;

        public static final String endpoint_export_descriptions =
                """
                        This endpoint is used to export a plan using a specific file transformer.
                        Choosing the transformer will determine the format of the export.
                        """;

    }

    public static final class Evaluator {

        public static final String endpoint_get_available_evaluators =
                """
                        This endpoint is used to fetch all the available evaluators.</br>
                        """;

        public static final String endpoint_rank_plans =
                """
                        This endpoint is used to rank a plan using a specific evaluator.</br>
                        """;

        public static final String endpoint_rank_descriptions =
                """
                        This endpoint is used to rank a description using a specific evaluator.</br>
                        """;
    }

    public static final class EntityDoi {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available entity dois.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Entity doi specific query parameters:</u>
                        
                        <ul>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>types:</b>
                            This is a list and determines which records we want to include in the response, based on their type.
                            The type can only be <i>DMP</i> currently. We add 0 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>dois:</b>
                            This is a list and determines which records we want to include in the response, based on their doi string.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "doi",
                                 "entityId",
                                 "entityType",
                                 "repositoryId",
                                 "updatedAt",
                                 "createdAt",
                                 "hash",
                                 "isActive",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"d6a8d517-e7a8-406c-9d47-66189e58236f",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11174908",
                                 "createdAt":"2024-05-10T13:21:26Z",
                                 "updatedAt":"2024-05-10T13:21:26Z",
                                 "isActive":1,
                                 "entityId":"139f4197-1145-4440-ad5a-4f00398da12e",
                                 "hash":"1715347286",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"0c486d94-a37f-4042-81a2-7698971cf30a",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11174466",
                                 "createdAt":"2024-05-10T11:43:46Z",
                                 "updatedAt":"2024-05-10T11:43:46Z",
                                 "isActive":1,
                                 "entityId":"4a8a6642-5de6-4de5-b257-5c119109607a",
                                 "hash":"1715341426",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"ad850e47-ce75-465f-ad29-d394dda531b1",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11161991",
                                 "createdAt":"2024-05-09T10:39:10Z",
                                 "updatedAt":"2024-05-09T10:39:10Z",
                                 "isActive":1,
                                 "entityId":"5089fa85-76e2-46e6-b7e5-82bf62f66d13",
                                 "hash":"1715251150",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"5c356559-872d-4f07-bafd-197c92df1564",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11152565",
                                 "createdAt":"2024-05-08T18:11:24Z",
                                 "updatedAt":"2024-05-08T18:11:24Z",
                                 "isActive":1,
                                 "entityId":"e36915dc-6496-4b35-b9c4-37576dc08389",
                                 "hash":"1715191884",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"05bb9602-b640-428b-a9b0-74025bece4ee",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11091371",
                                 "createdAt":"2024-04-30T08:58:34Z",
                                 "updatedAt":"2024-04-30T08:58:34Z",
                                 "isActive":1,
                                 "entityId":"8d1f813b-ad96-4449-a197-7dc828a6598d",
                                 "hash":"1714467514",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"9402523a-1836-4464-a9b2-4ac1c61493cc",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11082522",
                                 "createdAt":"2024-04-29T07:53:04Z",
                                 "updatedAt":"2024-04-29T07:53:04Z",
                                 "isActive":1,
                                 "entityId":"a092bbca-1dbe-4095-a3b9-ba65c7b85e13",
                                 "hash":"1714377184",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"85c58f3b-5063-4ddf-a1ab-426268c55565",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11082395",
                                 "createdAt":"2024-04-29T07:28:04Z",
                                 "updatedAt":"2024-04-29T07:28:04Z",
                                 "isActive":1,
                                 "entityId":"c0df9b41-5166-44a6-bdd9-12e0f7b6d5c7",
                                 "hash":"1714375684",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"72284e9e-cdf4-4d57-ac8d-59e3ed5bef9a",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11082383",
                                 "createdAt":"2024-04-29T07:25:07Z",
                                 "updatedAt":"2024-04-29T07:25:07Z",
                                 "isActive":1,
                                 "entityId":"e8e93ea4-e983-4299-a1ea-fd3a75c95ef5",
                                 "hash":"1714375507",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"ecf6fb11-58f0-42d5-a127-d263703dcd92",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11082161",
                                 "createdAt":"2024-04-29T06:51:43Z",
                                 "updatedAt":"2024-04-29T06:51:43Z",
                                 "isActive":1,
                                 "entityId":"6a4b7b4f-7507-4c52-9356-1f652f2cc430",
                                 "hash":"1714373503",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"4ce9b1e6-1e58-48c3-ae80-2ef09f1ef2d0",
                                 "entityType":0,
                                 "repositoryId":"Zenodo",
                                 "doi":"10.5281/zenodo.11081970",
                                 "createdAt":"2024-04-29T05:42:35Z",
                                 "updatedAt":"2024-04-29T05:42:35Z",
                                 "isActive":1,
                                 "entityId":"15e2c0ce-e65a-4c4b-9dfc-1393f042081c",
                                 "hash":"1714369355",
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":118
                        }
                        """;

    }

    public static final class Deposit {

        public static final String endpoint_get_available_repos =
                """
                        This endpoint is used to fetch all the available deposit repositories.
                        """;

        public static final String endpoint_deposit =
                """
                        This endpoint is used to deposit a plan in a repository.
                        """;

        public static final String endpoint_get_repository =
                """
                        This endpoint is used to get information about a specific repository.
                        """;

        public static final String endpoint_get_logo =
                """
                        This endpoint is used to fetch the logo url of a repository.
                        """;

    }

    public static final class Tag {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available tags.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Tag specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the tag entities that include the contents of the parameter in their labels will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>createdByIds:</b>
                            This is a list and contains the ids of the users who's tags we want to exclude from the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>tags:</b>
                            This is a list and determines which records we want to include in the response, based on their tag string.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>excludedTags:</b>
                            This is a list and determines which records we want to exclude from the response, based on their tag string.
                            <br/>If not present, no record is excluded.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "label",
                                 "createdAt",
                                 "updatedAt",
                                 "isActive",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"abd9d15e-4c70-4700-bce8-e90b8d99ecd5",
                                 "label":"PCM",
                                 "createdAt":"2024-06-27T13:52:48.417255Z",
                                 "updatedAt":"2024-06-27T13:52:48.417255Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"de185ef7-8d9a-458e-9af6-c7c1c9ea39e0",
                                 "label":"Phase change material",
                                 "createdAt":"2024-06-27T13:52:48.417255Z",
                                 "updatedAt":"2024-06-27T13:52:48.417255Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"7b74fc3e-b283-4203-8b2c-2b9999b96ce7",
                                 "label":"heat transfer enhancement in PCM",
                                 "createdAt":"2024-06-27T13:52:48.417255Z",
                                 "updatedAt":"2024-06-27T13:52:48.417255Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"d5cd61ff-26c8-4abb-a605-9400c8e6f453",
                                 "label":"energy",
                                 "createdAt":"2024-06-27T13:52:48.328879Z",
                                 "updatedAt":"2024-06-27T13:52:48.328879Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"a8ab80af-dcf4-4221-85d6-3c9ef304f52d",
                                 "label":" indicators",
                                 "createdAt":"2024-06-27T13:52:48.328879Z",
                                 "updatedAt":"2024-06-27T13:52:48.328879Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"b27cf507-296a-407e-87f2-e6245d60a622",
                                 "label":"district heating",
                                 "createdAt":"2024-06-27T13:52:48.328879Z",
                                 "updatedAt":"2024-06-27T13:52:48.328879Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"40eea91d-e3ef-4284-8a70-76cfb544e71b",
                                 "label":" energy resilience",
                                 "createdAt":"2024-06-27T13:52:48.328879Z",
                                 "updatedAt":"2024-06-27T13:52:48.328879Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"aa955ed9-7d40-486b-aec3-05515be36297",
                                 "label":"biomass",
                                 "createdAt":"2024-06-27T13:52:48.169676Z",
                                 "updatedAt":"2024-06-27T13:52:48.169676Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"a777b696-f861-4eba-8bb0-60951cf3cc8f",
                                 "label":"laboratory",
                                 "createdAt":"2024-06-27T13:52:48.169676Z",
                                 "updatedAt":"2024-06-27T13:52:48.169676Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"03ed72bc-af6e-47ea-b775-90674f50bdc2",
                                 "label":"pellets",
                                 "createdAt":"2024-06-27T13:52:48.169676Z",
                                 "updatedAt":"2024-06-27T13:52:48.169676Z",
                                 "isActive":1,
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":2789
                        }
                        """;

    }

    public static final class Reference {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available references.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Reference specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their labels, descriptions or the references will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>typeIds:</b>
                            This is a list and contains the ids of the types of the references we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>sourceTypes:</b>
                            This is a list and determines which records we want to include in the response, based on their resource type.
                            The resource type can only be <i>Internal</i> or <i>External</i>. We add 0 or 1 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "label",
                                 "createdAt",
                                 "updatedAt",
                                 "isActive",
                                 "reference",
                                 "abbreviation",
                                 "description",
                                 "source",
                                 "sourceType",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"016eba8f-1029-49cc-87eb-bfe17901d940",
                                 "label":"researcher",
                                 "reference":"12344",
                                 "abbreviation":"",
                                 "source":"Internal",
                                 "sourceType":0,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:55:40.330369Z",
                                 "updatedAt":"2024-07-04T12:55:40.332366Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"6d36fefe-94b4-4fce-9e4c-8f7421fdfd83",
                                 "label":"Grant",
                                 "reference":"grantId",
                                 "abbreviation":"",
                                 "source":"Internal",
                                 "sourceType":0,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:54:29.108764Z",
                                 "updatedAt":"2024-07-04T12:54:29.110765Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"1ad6d13b-1dd6-4189-a915-ba082984d185",
                                 "label":"Funder",
                                 "reference":"funderid",
                                 "abbreviation":"",
                                 "source":"Internal",
                                 "sourceType":0,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:54:28.936293Z",
                                 "updatedAt":"2024-07-04T12:54:28.939293Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"9c4c469c-3a86-405c-b09a-8d830957be75",
                                 "label":"Argentine Peso",
                                 "reference":"ARS",
                                 "source":"currencies",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:46:52.939455Z",
                                 "updatedAt":"2024-07-04T12:46:52.941187Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"7128614f-3f34-4844-9cbf-39bce1e12d7a",
                                 "label":"ADHDgene",
                                 "reference":"fairsharing_::1549",
                                 "source":"openaire",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:46:52.723995Z",
                                 "updatedAt":"2024-07-04T12:46:52.726995Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"3ba9da7d-5745-4686-a3fb-bc3cce4044b2",
                                 "label":"Cite Project",
                                 "reference":"cite_project",
                                 "source":"project",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:39:12.784789Z",
                                 "updatedAt":"2024-07-04T12:39:12.786810Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"84796db1-633d-4de7-af4d-b7fe5fad16c0",
                                 "label":"A Randomized, Double-Blind, Placebo-Controlled, Multi-Center Study to Evaluate the Efficacy of ManNAc in Subjects with GNE Myopathy (nih_________::5U01AR070498-04)",
                                 "reference":"nih_________::5U01AR070498-04",
                                 "source":"openaire",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-07-04T12:39:12.564228Z",
                                 "updatedAt":"2024-07-04T12:39:12.567428Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"067d98cb-c4af-4958-a197-0c248afc9fe9",
                                 "label":"FCT - Fundação para a Ciência e a Teconlogia",
                                 "reference":"//ror.org/00snfqn58",
                                 "source":"openaire",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-06-27T13:50:15.644509Z",
                                 "updatedAt":"2024-06-27T13:50:15.644509Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"8ea35687-229a-4c3b-95ec-b3c92b0963b0",
                                 "label":"BuG@Sbase",
                                 "reference":"fairsharing_::1556",
                                 "source":"openaire",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-06-27T13:50:14.273886Z",
                                 "updatedAt":"2024-06-27T13:50:14.273886Z",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"503037eb-a2d1-4bb7-aa23-afecebb3fee0",
                                 "label":"Amilya Agustina (orcid:0000-0002-9102-1506)",
                                 "reference":"0000-0002-9102-1506",
                                 "source":"orcid",
                                 "sourceType":1,
                                 "isActive":1,
                                 "createdAt":"2024-06-27T13:50:14.230997Z",
                                 "updatedAt":"2024-06-27T13:50:14.230997Z",
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":14270
                        }
                        """;

        public static final String endpoint_search =
                """
                        This endpoint is used to fetch all the available references.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_test =
                """
                        This endpoint is used to test reference results from external APIs configuration.<br/>
                        """;

        public static final String endpoint_search_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Reference specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their labels, descriptions or the references will be in the response.
                            </li>
                            <li><b>typeId:</b>
                            This is the type id of the references we want in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>key:</b>
                            This is the id of the external source (API) we want results from.
                            <br/>If not present, no external reference is included.
                            </li>
                            <li><b>dependencyReferences:</b>
                            This is a list and determines which records we want to include in the response, based on the references they depend on.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_search_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "hash",
                                 "label",
                                 "type",
                                 "type.id",
                                 "description",
                                 "definition.fields.code",
                                 "definition.fields.dataType",
                                 "definition.fields.value",
                                 "reference",
                                 "abbreviation",
                                 "source",
                                 "sourceType"
                              ]
                           },
                           "page":{
                              "size":100,
                              "offset":0
                           },
                           "typeId":"5b9c284f-f041-4995-96cc-fad7ad13289c",
                           "dependencyReferences":[
                             \s
                           ],
                           "order":{
                              "items":[
                                 "label"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_search_response_example =
                """
                        [
                             {
                                 "label": "A Randomized, Double-Blind, Placebo-Controlled, Multi-Center Study to Evaluate the Efficacy of ManNAc in Subjects with GNE Myopathy (nih_________::5U01AR070498-04)",
                                 "type": {
                                     "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                 },
                                 "description": "A Randomized, Double-Blind, Placebo-Controlled, Multi-Center Study to Evaluate the Efficacy of ManNAc in Subjects with GNE Myopathy",
                                 "definition": {
                                     "fields": [
                                         {
                                             "code": "referenceType",
                                             "dataType": 0,
                                             "value": "Grants"
                                         },
                                         {
                                             "code": "key",
                                             "dataType": 0,
                                             "value": "openaire"
                                         }
                                     ]
                                 },
                                 "reference": "nih_________::5U01AR070498-04",
                                 "source": "openaire",
                                 "sourceType": 1,
                                 "hash": ""
                             },
                             {
                                 "label": "A genome scale census of virulence factors in the major mould pathogen of human lungs, Aspergillus fumigatus (ukri________::1640253)",
                                 "type": {
                                     "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                 },
                                 "description": "A genome scale census of virulence factors in the major mould pathogen of human lungs, Aspergillus fumigatus",
                                 "definition": {
                                     "fields": [
                                         {
                                             "code": "referenceType",
                                             "dataType": 0,
                                             "value": "Grants"
                                         },
                                         {
                                             "code": "key",
                                             "dataType": 0,
                                             "value": "openaire"
                                         }
                                     ]
                                 },
                                 "reference": "ukri________::1640253",
                                 "source": "openaire",
                                 "sourceType": 1,
                                 "hash": ""
                             }
                        ]
                        """;

    }

    public static final class ReferenceType {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available reference types.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Reference type specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their names or their codes will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>codes:</b>
                            This is a list and determines which records we want to include in the response, based on their code.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "name",
                                 "code",
                                 "createdAt",
                                 "updatedAt",
                                 "isActive",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-createdAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                            "items":[
                               {
                                  "id":"7f4ea357-27ae-45bb-9588-551f1d926760",
                                  "name":"Select format(s)",
                                  "code":"Select format(s)",
                                  "isActive":1,
                                  "createdAt":"2024-06-27T13:06:56.928007Z",
                                  "updatedAt":"2024-06-27T13:06:56.931006Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"92186655-166c-463c-9898-82e25f2f010a",
                                  "name":"Metadata Services",
                                  "code":"Metadata Services",
                                  "isActive":1,
                                  "createdAt":"2024-06-27T13:06:56.845006Z",
                                  "updatedAt":"2024-06-27T13:06:56.854006Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"a3ce0fb2-d72c-48bb-b322-7401940cb802",
                                  "name":"Datasets",
                                  "code":"datasets",
                                  "isActive":1,
                                  "createdAt":"2023-11-17T10:26:55.332111Z",
                                  "updatedAt":"2024-05-01T10:34:07.029327Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"7eeffb98-58fb-4921-82ec-e27f32f8e738",
                                  "name":"Organisations",
                                  "code":"organisations",
                                  "isActive":1,
                                  "createdAt":"2023-11-17T10:13:15.873808Z",
                                  "updatedAt":"2024-02-16T15:35:47.874131Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"3d372db5-a456-45e6-a845-e41e1a8311f8",
                                  "name":"Projects",
                                  "code":"projects",
                                  "isActive":1,
                                  "createdAt":"2023-11-17T08:55:05.190807Z",
                                  "updatedAt":"2023-11-17T08:56:23.012619Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"5a2112e7-ea99-4cfe-98a1-68665e26726e",
                                  "name":"Researchers",
                                  "code":"researchers",
                                  "isActive":1,
                                  "createdAt":"2023-11-16T18:21:43.272982Z",
                                  "updatedAt":"2024-04-17T09:44:53.656849Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"9ec2000d-95c7-452e-b356-755fc8e2574c",
                                  "name":"Services",
                                  "code":"services",
                                  "isActive":1,
                                  "createdAt":"2023-11-16T17:57:22.081053Z",
                                  "updatedAt":"2024-02-16T09:07:13.944104Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"ab7cdd93-bea2-440d-880d-3846dad80b21",
                                  "name":"Taxonomies",
                                  "code":"taxonomies",
                                  "isActive":1,
                                  "createdAt":"2023-11-16T17:48:09.769599Z",
                                  "updatedAt":"2024-04-25T12:36:57.923984Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"8ec7556b-749d-4c4a-a4b9-43d064693795",
                                  "name":"Journals",
                                  "code":"journals",
                                  "isActive":1,
                                  "createdAt":"2023-11-16T17:40:12.811667Z",
                                  "updatedAt":"2024-02-16T09:09:22.816978Z",
                                  "belongsToCurrentTenant":true
                               },
                               {
                                  "id":"1e927daa-b856-443f-96da-22f325f7322f",
                                  "name":"Publication Repositories",
                                  "code":"pubRepositories",
                                  "isActive":1,
                                  "createdAt":"2023-11-16T17:17:40.882679Z",
                                  "updatedAt":"2024-05-01T11:52:50.297337Z",
                                  "belongsToCurrentTenant":true
                               }
                            ],
                            "count":17
                        }
                        """;

    }

    public static final class Lock {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the current entity locks.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>Lock specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the locks locking the provided target id will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>targetIds:</b>
                            This is a list and contains the ids of the locked targets of the locks we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>userIds:</b>
                            This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>targetTypes:</b>
                            This is a list and determines which records we want to include in the response, based on their target type.
                            The target type can be <i>Plan</i>, <i>Description</i>, <i>PlanBlueprint</i> or <i>DescriptionTemplate</i>. We add 0, 1, 2 or 3 to the list respectively.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "target",
                                 "targetType",
                                 "lockedAt",
                                 "lockedBy.name",
                                 "touchedAt",
                                 "hash",
                                 "belongsToCurrentTenant"
                              ]
                           },
                           "metadata":{
                              "countAll":true
                           },
                           "page":{
                              "offset":0,
                              "size":10
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "-lockedAt"
                              ]
                           }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"d0423a02-abe8-4210-8ee3-504deb79d8c6",
                                 "target":"39fabf73-546c-49a3-8789-6401f65d56b6",
                                 "targetType":1,
                                 "lockedBy":{
                                    "name":"admin admin"
                                 },
                                 "lockedAt":"2024-06-28T08:47:03.784241Z",
                                 "touchedAt":"2024-07-04T11:01:10.762955Z",
                                 "hash":"1720090870",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"74b2ae7e-0a28-4ebc-aec9-3f849ccb3e60",
                                 "target":"37701076-e0ff-4e4f-95aa-9f3d6a23083a",
                                 "targetType":2,
                                 "lockedBy":{
                                    "name":"admin admin"
                                 },
                                 "lockedAt":"2024-07-04T08:29:46.591493Z",
                                 "touchedAt":"2024-07-04T08:33:13.451444Z",
                                 "hash":"1720081993",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"642d6756-bf62-4555-9ca5-bec50c5cdb85",
                                 "target":"212342fe-ab6f-4604-b80e-ac23aca93c76",
                                 "targetType":3,
                                 "lockedBy":{
                                    "name":"admin admin"
                                 },
                                 "lockedAt":"2024-07-02T11:36:17.926775Z",
                                 "touchedAt":"2024-07-02T11:39:24.278433Z",
                                 "hash":"1719920364",
                                 "belongsToCurrentTenant":true
                              },
                              {
                                 "id":"4a7fd0b4-1b2e-4148-a5fc-152f73caa7e5",
                                 "target":"0e58f8b7-a91e-432a-8de2-edf03679c313",
                                 "targetType":1,
                                 "lockedBy":{
                                    "name":"admin"
                                 },
                                 "lockedAt":"2024-07-01T11:16:14.806474Z",
                                 "touchedAt":"2024-07-01T11:23:21.922533Z",
                                 "hash":"1719833001",
                                 "belongsToCurrentTenant":true
                              }
                           ],
                           "count":4
                        }
                        """;

    }

    public static final class User {

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available users.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_plan_associated =
                """
                        This endpoint is used to fetch all the available users.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body =
                """
                        Let's explore the options this object gives us.
                        
                        ### <u>General query parameters:</u>
                        
                        <ul>
                            <li><b>page:</b>
                            This is an object controlling the pagination of the results. It contains two properties.
                            </li>
                            <ul>
                                <li><b>offset:</b>
                                How many records to omit.
                                </li>
                                <li><b>size:</b>
                                How many records to include in each page.
                                </li>
                            </ul>
                        </ul>
                        
                        For example, if we want the third page, and our pages to contain 15 elements, we would pass the following object:
                        
                        ```JSON
                        {
                            "offset": 30,
                            "size": 15
                        }
                        ```
                        
                        <ul>
                            <li><b>order:</b>
                            This is an object controlling the ordering of the results.
                            It contains a list of strings called <i>items</i> with the names of the properties to use.
                            <br/>If the name of the property is prefixed with a <b>'-'</b>, the ordering direction is <b>DESC</b>. Otherwise, it is <b>ASC</b>.
                            </li>
                        </ul>
                        
                        For example, if we wanted to order based on the field 'createdAt' in descending order, we would pass the following object:
                        
                        ```JSON
                        {
                            "items": [
                                "-createdAt"
                            ],
                        }
                        ```
                        
                        <ul>
                            <li><b>metadata:</b>
                            This is an object containing metadata for the request. There is only one available option.
                            <ul>
                                <li><b>countAll:</b>
                                If this is set to true, the count property included in the response will account for all the records regardless the pagination,
                                with all the rest of filtering options applied of course.
                                Otherwise, if it is set to false or not present, only the returned results will be counted.
                                <br/>The first option is useful for the UI clients to calculate how many result pages are available.
                                </li>
                            </ul>
                            </li>
                            <li><b>project:</b>
                            This is an object controlling the data projection of the results.
                            It contains a list of strings called <i>fields</i> with the names of the properties to project.
                            <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.
                            </li>
                        </ul>
                        
                        ### <u>User specific query parameters:</u>
                        
                        <ul>
                            <li><b>like:</b>
                            If there is a like parameter present in the query, only the users that include the contents of the parameter in their names will be in the response.
                            </li>
                            <li><b>ids:</b>
                            This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.
                            </li>
                            <li><b>excludedIds:</b>
                            This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                            <li><b>emails:</b>
                            This is a list and determines which records we want to include in the response, based on their emails.
                            <br/>If not present, every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                                "project":{
                                   "fields":[
                                      "id",
                                      "name",
                                      "contacts.id",
                                      "contacts.type",
                                      "contacts.value",
                                      "globalRoles.id",
                                      "globalRoles.role",
                                      "tenantRoles.id",
                                      "tenantRoles.role",
                                      "additionalInfo.avatarUrl",
                                      "updatedAt",
                                      "createdAt",
                                      "hash",
                                      "isActive"
                                   ]
                                },
                                "metadata":{
                                   "countAll":true
                                },
                                "page":{
                                   "offset":0,
                                   "size":10
                                },
                                "isActive":[
                                   1
                                ],
                                "order":{
                                   "items":[
                                      "-createdAt"
                                   ]
                                }
                             }
                        """;

        public static final String endpoint_query_plan_associated_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "name",
                                 "email"
                              ]
                           },
                           "page":{
                              "size":100,
                              "offset":0
                           },
                           "isActive":[
                              1
                           ],
                           "order":{
                              "items":[
                                 "name"
                              ]
                           },
                           "like":"user%"
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"fc97ad11-2c73-4fc4-835e-e1ca0cf7b918",
                                 "name":"user5 user5",
                                 "createdAt":"2024-07-03T09:59:05.005425Z",
                                 "updatedAt":"2024-07-03T09:59:05.005425Z",
                                 "isActive":1,
                                 "hash":"1720000745",
                                 "additionalInfo":{
                                   \s
                                 },
                                 "contacts":[
                                    {
                                       "id":"8b0972d2-e9d8-4c61-b69b-907ee452dade",
                                       "value":"user5@user5.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "globalRoles":[
                                    {
                                       "id":"50b8e8e8-eb02-40cb-aa6f-a99eb0f48721",
                                       "role":"User",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "tenantRoles":[
                                    {
                                       "id":"cc0307e5-4421-4e46-b7a1-b1326e6e786b",
                                       "role":"TenantUser",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"b494c989-60f1-4584-bc52-b56c40c66ade",
                                 "name":"installationadmin installationadmin",
                                 "createdAt":"2024-06-28T13:23:24.957340Z",
                                 "updatedAt":"2024-06-28T13:23:24.957340Z",
                                 "isActive":1,
                                 "hash":"1719581004",
                                 "additionalInfo":{
                                   \s
                                 },
                                 "contacts":[
                                    {
                                       "id":"cd281a02-f851-400f-ba7f-391e16884051",
                                       "value":"installationadmin@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "globalRoles":[
                                    {
                                       "id":"1d4088c3-7d42-4227-93d9-ee46ead5f500",
                                       "role":"InstallationAdmin",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "tenantRoles":[
                                    {
                                       "id":"b606b428-f404-4232-81fa-a313fcbab25a",
                                       "role":"TenantUser",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"5119e0a6-53ee-4cad-ae58-aa3c1fc79683",
                                 "name":"user3 user3",
                                 "createdAt":"2024-06-28T11:23:22.962858Z",
                                 "updatedAt":"2024-06-28T11:23:22.962858Z",
                                 "isActive":1,
                                 "hash":"1719573802",
                                 "additionalInfo":{
                                   \s
                                 },
                                 "contacts":[
                                    {
                                       "id":"18f1f3f2-77d6-4258-a7d0-1623d7282b82",
                                       "value":"user3@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "globalRoles":[
                                    {
                                       "id":"116ec22c-3b3a-44ae-9de0-be31c8d621c2",
                                       "role":"User",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "tenantRoles":[
                                    {
                                       "id":"618b266b-ecdb-46cd-a5c5-686dc76bba12",
                                       "role":"TenantUser",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"d1873841-3ae3-4a1c-8cfc-841327552313",
                                 "name":"dmproot dmproot",
                                 "createdAt":"2024-06-28T07:19:37.150149Z",
                                 "updatedAt":"2024-06-28T07:19:37.150149Z",
                                 "isActive":1,
                                 "hash":"1719559177",
                                 "additionalInfo":{
                                   \s
                                 },
                                 "contacts":[
                                    {
                                       "id":"20d108c6-8277-40d0-a2b8-e6d8c9c332f0",
                                       "value":"opencdmp@cite.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "globalRoles":[
                                    {
                                       "id":"209b7b67-5374-4b11-a1cf-ecfba6da5f16",
                                       "role":"Admin",
                                       "user":{
                                         \s
                                       }
                                    },
                                    {
                                       "id":"5d7ee923-5456-456c-b46f-b53458b0e10a",
                                       "role":"User",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "tenantRoles":[
                                    {
                                       "id":"4a14b1fd-2e35-4817-89fa-9d8bf741aee7",
                                       "role":"TenantUser",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"890667ae-7efd-49d9-8ab5-3d48b84a48d1",
                                 "name":"admin admin",
                                 "createdAt":"2024-06-28T07:07:18.589432Z",
                                 "updatedAt":"2024-06-28T07:07:18.589432Z",
                                 "isActive":1,
                                 "hash":"1719558438",
                                 "additionalInfo":{
                                   \s
                                 },
                                 "contacts":[
                                    {
                                       "id":"9739739b-b5d9-4e13-bc47-637b3760b340",
                                       "value":"admin@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "globalRoles":[
                                    {
                                       "id":"718dd2d0-cbda-4b33-b8be-a25ec30a53f3",
                                       "role":"User",
                                       "user":{
                                         \s
                                       }
                                    },
                                    {
                                       "id":"c2c4129e-dcec-448a-8e7c-e4dd7cbc3d9d",
                                       "role":"Admin",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ],
                                 "tenantRoles":[
                                    {
                                       "id":"6961542d-6ea7-4064-a0ad-20d82896b9de",
                                       "role":"TenantAdmin",
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"1f709343-353b-4787-a8e0-f71020b53f94",
                                 "name":"user5847",
                                 "createdAt":"2024-06-26T22:46:43Z",
                                 "updatedAt":"2024-06-26T22:46:43Z",
                                 "isActive":1,
                                 "hash":"1719442003",
                                 "additionalInfo":{
                                    "avatarUrl":"null"
                                 },
                                 "contacts":[
                                    {
                                       "id":"b8c17fa6-c8e1-49d8-9c7b-173dad43d995",
                                       "value":"user5847@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"ac633bd9-ac0b-4258-adc5-24246e1dacbc",
                                 "name":"user5846",
                                 "createdAt":"2024-06-26T22:08:44Z",
                                 "updatedAt":"2024-06-26T22:08:44Z",
                                 "isActive":1,
                                 "hash":"1719439724",
                                 "additionalInfo":{
                                    "avatarUrl":"null"
                                 },
                                 "contacts":[
                                    {
                                       "id":"0126a7d6-c2f3-4f2e-85cc-9c999d74fa85",
                                       "value":"user5846@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"2b4e633b-5451-46fb-a1ad-34c1f8fb8fe7",
                                 "name":"user5844",
                                 "createdAt":"2024-06-26T17:04:01Z",
                                 "updatedAt":"2024-06-26T17:04:01Z",
                                 "isActive":1,
                                 "hash":"1719421441",
                                 "additionalInfo":{
                                    "avatarUrl":"null"
                                 },
                                 "contacts":[
                                    {
                                       "id":"138b9b60-8bfb-4900-8f0f-32dd70c8841c",
                                       "value":"user5844@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"d9d0ae8b-8037-403e-a155-28d2387d6d7f",
                                 "name":"user5842",
                                 "createdAt":"2024-06-26T13:38:58Z",
                                 "updatedAt":"2024-06-26T13:38:58Z",
                                 "isActive":1,
                                 "hash":"1719409138",
                                 "additionalInfo":{
                                    "avatarUrl":"null"
                                 },
                                 "contacts":[
                                    {
                                       "id":"12bb3f67-941c-4731-8de0-b8d43ff6e17a",
                                       "value":"user5842@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              },
                              {
                                 "id":"3748e22c-3760-4ada-88ef-8addd247fc76",
                                 "name":"user5841",
                                 "createdAt":"2024-06-26T13:19:52Z",
                                 "updatedAt":"2024-06-26T13:22:19Z",
                                 "isActive":1,
                                 "hash":"1719408139",
                                 "additionalInfo":{
                                    "avatarUrl":"null"
                                 },
                                 "contacts":[
                                    {
                                       "id":"d536278f-9714-43f8-846c-cb5c6bcf2f59",
                                       "value":"user5841@dmp.gr",
                                       "type":0,
                                       "ordinal":0,
                                       "user":{
                                         \s
                                       }
                                    }
                                 ]
                              }
                           ],
                           "count":5821
                        }
                        """;

        public static final String endpoint_query_plan_associated_response_example =
                """
                        {
                           "items":[
                              {
                                 "id":"d26916c6-8763-450e-9048-b06e1114d0b4",
                                 "name":"user3 user3",
                                 "email":"user3@dmp.gr"
                              },
                              {
                                 "id":"02832fb6-0b12-469f-a886-7685406959d4",
                                 "name":"user4",
                                 "email":"user4@dmp.gr"
                              }
                           ],
                           "count":2
                        }
                        """;

    }

    public static final class Principal {

    }

}
