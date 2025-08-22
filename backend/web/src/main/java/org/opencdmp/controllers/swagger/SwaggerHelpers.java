package org.opencdmp.controllers.swagger;

public final class SwaggerHelpers {

    public static final class Commons {

        public static final String fieldset_description =
                """
                        A list of the properties you wish to include in the response, similar to the 'project' attribute on queries.
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

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available plans.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_public_query =
                """
                        This endpoint is used to fetch all the available public published plans.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_public_query_request_body =
                """
                        For public plans we used the same query parameters as plans queries
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
                            <li><b>statusIds:</b>
                            This is a list and determines which records we want to include in the response, based on if they have one of the specific status ids.. <br/>If empty, every record is included.
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
                            "project": {
                                "fields": [
                                    "id",
                                    "label",
                                    "status.id",
                                    "status.name",
                                    "status.internalStatus",
                                    "status.definition.availableActions",
                                    "versionStatus",
                                    "groupId",
                                    "description",
                                    "language",
                                    "accessType",
                                    "isActive",
                                    "version",
                                    "updatedAt",
                                    "publicAfter",
                                    "creator",
                                    "hash",
                                    "belongsToCurrentTenant",
                                    "authorizationFlags.EditPlan",
                                    "authorizationFlags.DeletePlan",
                                    "authorizationFlags.EditDescription",
                                    "authorizationFlags.ExportPlan",
                                    "statusAuthorizationFlags.Edit",
                                    "properties.planBlueprintValues.fieldId",
                                    "properties.planBlueprintValues.fieldValue",
                                    "properties.planBlueprintValues.dateValue",
                                    "properties.planBlueprintValues.numberValue",
                                    "properties.contacts.firstName",
                                    "properties.contacts.lastName",
                                    "properties.contacts.email",
                                    "PlanEditorDescriptionsLookupFields",
                                    "planUsers.id",
                                    "planUsers.user.id",
                                    "planUsers.user.name",
                                    "planUsers.role",
                                    "planUsers.sectionId",
                                    "planUsers.isActive",
                                    "planReferences.id",
                                    "planReferences.isActive",
                                    "planReferences.data.blueprintFieldId",
                                    "planReferences.reference.id",
                                    "planReferences.reference.label",
                                    "planReferences.reference.type.id",
                                    "planReferences.reference.source",
                                    "planReferences.reference.reference",
                                    "planReferences.reference.sourceType",
                                    "planDescriptionTemplates.id",
                                    "planDescriptionTemplates.sectionId",
                                    "planDescriptionTemplates.descriptionTemplateGroupId",
                                    "planDescriptionTemplates.isActive",
                                    "planDescriptionTemplates.descriptionTemplates",
                                    "planDescriptionTemplates.currentDescriptionTemplate.id",
                                    "planDescriptionTemplates.currentDescriptionTemplate.label",
                                    "planDescriptionTemplates.currentDescriptionTemplate.version",
                                    "planDescriptionTemplates.currentDescriptionTemplate.type.name",
                                    "planDescriptionTemplates.currentDescriptionTemplate.type.id",
                                    "entityDois.id",
                                    "entityDois.repositoryId",
                                    "entityDois.doi",
                                    "entityDois.isActive",
                                    "availableStatuses.id",
                                    "availableStatuses.name",
                                    "availableStatuses.internalStatus",
                                    "availableStatuses.action",
                                    "blueprint.id",
                                    "blueprint.definition",
                                    "blueprint.definition.sections.id",
                                    "blueprint.definition.sections.label",
                                    "blueprint.definition.sections.ordinal",
                                    "blueprint.definition.sections.description",
                                    "blueprint.definition.sections.canEditDescriptionTemplates",
                                    "blueprint.definition.sections.prefillingSourcesEnabled",
                                    "blueprint.definition.sections.prefillingSources.id",
                                    "blueprint.definition.sections.hasTemplates",
                                    "blueprint.definition.sections.descriptionTemplates.descriptionTemplate.groupId",
                                    "blueprint.definition.sections.descriptionTemplates.minMultiplicity",
                                    "blueprint.definition.sections.descriptionTemplates.maxMultiplicity",
                                    "blueprint.definition.sections.fields.id",
                                    "blueprint.definition.sections.fields.category",
                                    "blueprint.definition.sections.fields.label",
                                    "blueprint.definition.sections.fields.placeholder",
                                    "blueprint.definition.sections.fields.description",
                                    "blueprint.definition.sections.fields.required",
                                    "blueprint.definition.sections.fields.ordinal",
                                    "blueprint.definition.sections.fields.dataType",
                                    "blueprint.definition.sections.fields.systemFieldType",
                                    "blueprint.definition.sections.fields.referenceType.id",
                                    "blueprint.definition.sections.fields.referenceType.name",
                                    "blueprint.definition.sections.fields.referenceType.code",
                                    "blueprint.definition.sections.fields.multipleSelect",
                                    "blueprint.definition.sections.fields.referenceType.definition.sources.referenceTypeDependencies.id",
                                    "blueprint.definition.sections.fields.maxFileSizeInMB",
                                    "blueprint.definition.sections.fields.types.label",
                                    "blueprint.definition.sections.fields.types.value",
                                    "descriptions.id",
                                    "descriptions.label",
                                    "descriptions.status.id",
                                    "descriptions.status.name",
                                    "descriptions.status.internalStatus",
                                    "descriptions.descriptionTemplate.groupId",
                                    "descriptions.planDescriptionTemplate.sectionId",
                                    "descriptions.isActive"
                                ]
                            },
                            "metadata": {
                                "countAll": true
                            },
                            "page": {
                                "offset": 0,
                                "size": 5
                            },
                            "isActive": [
                                1
                            ],
                            "order": {
                                "items": [
                                    "-updatedAt"
                                ]
                            },
                            "groupIds": null
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "8366ea9e-6f14-4719-9d69-0afa50658c3c",
                              "label": "CHIST-ERA Plan",
                              "version": 1,
                              "status": {
                                "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    1,
                                    2
                                  ],
                                  "matIconName": "edit",
                                  "storageFile": {}
                                }
                              },
                              "versionStatus": 2,
                              "groupId": "6c6f90d0-5179-4951-a046-f0b9685c7fb3",
                              "updatedAt": "2025-05-15T08:27:59.340189Z",
                              "isActive": 1,
                              "blueprint": {
                                "id": "4b2fc3b4-5244-45aa-bbe5-ccc1d16201e5",
                                "label": "CHIST-ERA",
                                "definition": {
                                  "sections": [
                                    {
                                      "id": "32b5443d-6eba-9052-05a8-fa9512f366ac",
                                      "label": "Administrative Information",
                                      "hasTemplates": false
                                    },
                                    {
                                      "id": "775c1ad3-84ea-df57-f6be-16098752e0b4",
                                      "label": "Data Management",
                                      "hasTemplates": true,
                                      "descriptionTemplates": [
                                        {
                                          "descriptionTemplate": {
                                            "groupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a"
                                          }
                                        },
                                        {
                                          "descriptionTemplate": {
                                            "groupId": "7a9bdf03-8ff7-42c9-80d5-d742f20bcccc"
                                          }
                                        }
                                      ]
                                    },
                                    {
                                      "id": "232c8265-207a-938b-569c-0f23967d70a8",
                                      "label": "Software Management",
                                      "hasTemplates": true,
                                      "descriptionTemplates": [
                                        {
                                          "descriptionTemplate": {
                                            "groupId": "5ac06da3-4f6a-4964-a49d-23cdafec1a00"
                                          }
                                        },
                                        {
                                          "descriptionTemplate": {
                                            "groupId": "6654faf4-c1b8-46a5-a970-e12cf9c03bad"
                                          }
                                        }
                                      ]
                                    }
                                  ]
                                }
                              },
                              "hash": "1747297679",
                              "planUsers": [
                                {
                                  "id": "726ac244-ef55-44e6-90eb-dcb1486d01e6",
                                  "plan": {
                                    "id": "8366ea9e-6f14-4719-9d69-0afa50658c3c"
                                  },
                                  "user": {
                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                  },
                                  "role": 0,
                                  "isActive": 1
                                }
                              ],
                              "planDescriptionTemplates": [
                                {
                                  "plan": {},
                                  "sectionId": "775c1ad3-84ea-df57-f6be-16098752e0b4",
                                  "descriptionTemplateGroupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a",
                                  "isActive": 1
                                },
                                {
                                  "plan": {},
                                  "sectionId": "775c1ad3-84ea-df57-f6be-16098752e0b4",
                                  "descriptionTemplateGroupId": "7a9bdf03-8ff7-42c9-80d5-d742f20bcccc",
                                  "isActive": 1
                                },
                                {
                                  "plan": {},
                                  "sectionId": "232c8265-207a-938b-569c-0f23967d70a8",
                                  "descriptionTemplateGroupId": "5ac06da3-4f6a-4964-a49d-23cdafec1a00",
                                  "isActive": 1
                                },
                                {
                                  "plan": {},
                                  "sectionId": "232c8265-207a-938b-569c-0f23967d70a8",
                                  "descriptionTemplateGroupId": "6654faf4-c1b8-46a5-a970-e12cf9c03bad",
                                  "isActive": 1
                                }
                              ],
                              "authorizationFlags": [
                                "ClonePlan",
                                "EditPlan",
                                "InvitePlanUsers",
                                "FinalizePlan",
                                "AssignPlanUsers",
                                "DeletePlan",
                                "ExportPlan",
                                "CreateNewVersionPlan"
                              ],
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "e933e328-5782-41b9-9ce7-770981205de6",
                              "label": "Dmp Default Blueprint",
                              "version": 1,
                              "status": {
                                "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    1,
                                    2
                                  ],
                                  "matIconName": "edit",
                                  "storageFile": {}
                                }
                              },
                              "versionStatus": 2,
                              "groupId": "4b38a67d-335a-4599-aa80-3308d378c648",
                              "updatedAt": "2025-05-14T11:59:03.636908Z",
                              "isActive": 1,
                              "blueprint": {
                                "id": "6db28659-36e5-4d5a-bf5e-222822d31768",
                                "label": "Dmp_Default_Blueprint.xml",
                                "definition": {
                                  "sections": [
                                    {
                                      "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                      "label": "Main Info",
                                      "hasTemplates": false
                                    },
                                    {
                                      "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                      "label": "Funding",
                                      "hasTemplates": false
                                    },
                                    {
                                      "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                      "label": "License",
                                      "hasTemplates": false
                                    },
                                    {
                                      "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                      "label": "Templates",
                                      "hasTemplates": true
                                    }
                                  ]
                                }
                              },
                              "hash": "1747223943",
                              "planUsers": [
                                {
                                  "id": "fdf9208e-1a27-4541-ad0a-32c32a8042ca",
                                  "plan": {
                                    "id": "e933e328-5782-41b9-9ce7-770981205de6"
                                  },
                                  "user": {
                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                  },
                                  "role": 0,
                                  "isActive": 1
                                }
                              ],
                              "authorizationFlags": [
                                "ClonePlan",
                                "EditPlan",
                                "InvitePlanUsers",
                                "FinalizePlan",
                                "AssignPlanUsers",
                                "DeletePlan",
                                "ExportPlan",
                                "CreateNewVersionPlan"
                              ],
                              "belongsToCurrentTenant": true
                            },
                          ],
                          "count": 3561
                        }
                        """;

        public static final String endpoint_public_query_request_body_example =
                """
                        {
                           "project": {
                             "fields": [
                               "id",
                               "label",
                               "description",
                               "isActive",
                               "status.id",
                               "status.name",
                               "status.internalStatus",
                               "status.definition.statusColor",
                               "status.definition.availableActions",
                               "status.definition.matIconName",
                               "status.definition.storageFile.id",
                               "accessType",
                               "version",
                               "versionStatus",
                               "groupId",
                               "updatedAt",
                               "belongsToCurrentTenant",
                               "finalizedAt",
                               "hash",
                               "tenantId",
                               "entityDois",
                               "entityDois.id",
                               "entityDois.doi",
                               "authorizationFlags.CreateNewVersionPlan",
                               "authorizationFlags.DeletePlan",
                               "authorizationFlags.ClonePlan",
                               "authorizationFlags.FinalizePlan",
                               "authorizationFlags.ExportPlan",
                               "authorizationFlags.InvitePlanUsers",
                               "authorizationFlags.AssignPlanUsers",
                               "authorizationFlags.EditPlan",
                               "descriptions.id",
                               "descriptions.label",
                               "descriptions.status.id",
                               "descriptions.status.name",
                               "descriptions.status.internalStatus",
                               "descriptions.descriptionTemplate.groupId",
                               "descriptions.planDescriptionTemplate.sectionId",
                               "descriptions.isActive",
                               "blueprint.id",
                               "blueprint.label",
                               "blueprint.definition.sections.id",
                               "blueprint.definition.sections.label",
                               "blueprint.definition.sections.hasTemplates",
                               "blueprint.definition.sections.descriptionTemplates.descriptionTemplate.groupId",
                               "planUsers.id",
                               "planUsers.user.id",
                               "planUsers.role",
                               "planUsers.plan.id",
                               "planUsers.isActive",
                               "planReferences.id",
                               "planReferences.reference.id",
                               "planReferences.reference.label",
                               "planReferences.reference.type.id",
                               "planReferences.isActive",
                               "planDescriptionTemplates.descriptionTemplateGroupId",
                               "planDescriptionTemplates.sectionId",
                               "planDescriptionTemplates.descriptionTemplateGroupId",
                               "planDescriptionTemplates.isActive"
                             ]
                           },
                           "metadata": {
                             "countAll": true
                           },
                           "page": {
                             "offset": 0,
                             "size": 5
                           },
                           "isActive": [
                             1
                           ],
                           "order": {
                             "items": [
                               "-updatedAt"
                             ]
                           },
                           "groupIds": null
                         }
                        """;

        public static final String endpoint_public_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "a2af5e24-0302-4a67-9c4e-8c04787b028d",
                              "label": "Selective DNA markers for Apera spica-venti",
                              "version": 0,
                              "description": "Creation, backup, storage and availability of the dataset of candidate selective DNA markers for Apera spica-venti",
                              "updatedAt": "2024-10-10T10:42:59Z",
                              "finalizedAt": "2024-10-10T13:42:59Z",
                              "status": {
                                "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                "name": "Finalized",
                                "internalStatus": 1,
                                "definition": {
                                  "availableActions": [
                                    0,
                                    1,
                                    2
                                  ]
                                }
                              },
                              "groupId": "b2764e17-8252-4706-9911-65addbd2f9bd",
                              "accessType": 0,
                              "planUsers": [
                                {
                                  "id": "a9feb692-7487-42e9-b81c-e158d33a63a1",
                                  "plan": {
                                    "id": "a2af5e24-0302-4a67-9c4e-8c04787b028d"
                                  },
                                  "user": {
                                    "id": "bf20243d-601e-4b62-b682-4d7aa828bd7c"
                                  },
                                  "role": 0
                                }
                              ],
                              "planReferences": [
                                {
                                  "id": "409f39db-1ece-42be-921e-fe1b404b89e5",
                                  "plan": {},
                                  "reference": {
                                    "id": "f33f56fd-73f7-4f50-9c9c-b63e4817d7f1",
                                    "label": "Univeristy of Latvia",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a0f65305-71fa-4fd4-b784-6e6fb8382a78",
                                  "plan": {},
                                  "reference": {
                                    "id": "0ea7ad8b-879e-4fce-a5e3-c0f3cfe780a6",
                                    "label": "Anete Boroduske (orcid:0000-0003-0077-9811)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "3aa91778-1a48-4813-8b8d-e89d01e9873e",
                                  "plan": {},
                                  "reference": {
                                    "id": "f9380468-271f-40a8-a6e0-17c220f59341",
                                    "label": "Jevgenija Necajeva (orcid:0000-0002-0828-9721)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "5bd392ce-a3b4-4cad-a9cc-8d8d18a6e3ea",
                                  "plan": {},
                                  "reference": {
                                    "id": "f180e09f-8a4a-483c-97d9-7e48e63f9bb3",
                                    "label": "Elza Kaktiņa (orcid:0009-0008-5745-6160)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "93dcc948-f701-4265-b2c6-1e18afefe0c5",
                                  "plan": {},
                                  "reference": {
                                    "id": "d72993d3-ffc9-48f4-8c37-9b76f813333e",
                                    "label": "Brandon Sinn (orcid:0000-0002-5596-6895)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2a0c0a0f-7daa-477a-84ea-9430d9ae964c",
                                  "plan": {},
                                  "reference": {
                                    "id": "26c3cd3a-626a-48bb-bf9e-0c75f6bdcbb7",
                                    "label": "DNS marķieru izstrāde parastās rudzusmilgas (Apera spica-venti) sēklu noteikšanai augsnes sēklu bankā (lzp-2023/1-0265)",
                                    "type": {
                                      "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "9b178cf6-4e49-443d-b653-369e5149e9ae",
                                  "plan": {},
                                  "reference": {
                                    "id": "ff16675f-92e6-4fd3-9c74-23de5dc9f0c6",
                                    "label": "Latvian Council of Sciences",
                                    "type": {
                                      "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                    }
                                  },
                                  "isActive": 1
                                }
                              ],
                              "descriptions": [
                                {
                                  "id": "f7c154f9-3653-44e1-8e08-365f58141e19",
                                  "label": "Sequencing data",
                                  "status": {
                                    "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                    "name": "Finalized",
                                    "internalStatus": 1
                                  },
                                  "planDescriptionTemplate": {},
                                  "descriptionTemplate": {
                                    "groupId": "cc785a69-2d90-46c4-baba-3bedbfc4d997"
                                  },
                                  "plan": {}
                                }
                              ],
                              "entityDois": [
                                {
                                  "id": "0ac95ee3-04a9-4ff2-b3f2-2c669af5cae1",
                                  "doi": "10.5281/zenodo.13913158"
                                }
                              ]
                            },
                            {
                              "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa",
                              "label": "REMAP - REusable MAsk Patterning",
                              "version": 0,
                              "description": "REMAP envisions a radically new and green surface patterning technique based on the spontaneous formation of reusable magnetic masks. Such masks are possible using fully adjustable and reversible interactions of &#8220;magnetorheological electrolytes&#8221; (MRE) on a substrate and microstructured magnetic fields generated by a permanent array of electromagnets below the substrate. By selectively activating each micro-electromagnet, it is possible to modulate the intensity and shape of the magnetic field (hence the mask) over space and time.<br><br>This way, REMAP enables high-throughput area-selective additive and subtractive patterning on a surface at room temperature and pressure. Furthermore, the newly devised MREs and the tuneable magnetic array developed within REMAP will pave the way to a plethora of future applications from lab-on-a-chip biomedicine, NMR analysis and smart fluids for robotic space exploration.",
                              "updatedAt": "2024-10-08T12:56:24Z",
                              "finalizedAt": "2024-10-08T15:56:24Z",
                              "status": {
                                "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                "name": "Finalized",
                                "internalStatus": 1,
                                "definition": {
                                  "availableActions": [
                                    0,
                                    1,
                                    2
                                  ]
                                }
                              },
                              "groupId": "25ffbcae-432a-4fd9-a280-0cc1c7f30d10",
                              "accessType": 0,
                              "planUsers": [
                                {
                                  "id": "518a7c10-d6f9-4f8c-99d8-f6e33dea83d0",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "0073b791-8ad7-4fda-88b6-7a51391f0c6a"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "68ba200c-2a57-4427-80a3-f32ebfd57f52",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "7d26ba1e-1c8e-4398-8834-d29a7856441c"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "6e84af75-41af-4a8e-9b0d-97cc6a1749b4",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "c9c55871-62ea-48b5-8b06-a5c8ddb55de3"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "7ca342a9-8c2d-4b1d-9438-57c1374e6ce7",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "83780596-84f9-498e-91b2-3639409a2a4e"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "7de21834-a9b2-4484-bf01-5ed41259ab61",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "61377735-c943-40ff-afd0-8c2fb6a69908"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "9cf6a694-89f7-4258-be7a-1e7c7d937c55",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "5d5fe59a-9194-471b-b936-64ae912aa4d7"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "9f4580d7-c9f0-4195-b63b-ca77def43af6",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "809ea801-2731-449d-997c-de1d11cb8893"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "b16f4aac-7439-4752-9ae1-bb11e8da2368",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "23ad58bf-2ce0-47fb-80f3-7bb86f39aa58"
                                  },
                                  "role": 1
                                },
                                {
                                  "id": "b511679d-5504-4b5c-9880-550af2ca4b54",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "a59fa9e2-2c39-4b16-b41a-4d1f07d36f8e"
                                  },
                                  "role": 0
                                },
                                {
                                  "id": "d7f53d0c-587d-41e1-ac7a-caf3f460caad",
                                  "plan": {
                                    "id": "42eff5b3-e2f6-4e43-ae65-b7212f9790fa"
                                  },
                                  "user": {
                                    "id": "585901e3-da03-49d3-804f-b7ebe30b06ce"
                                  },
                                  "role": 1
                                }
                              ],
                              "planReferences": [
                                {
                                  "id": "e1342ee1-8e0b-46bb-ad43-cd772abf5604",
                                  "plan": {},
                                  "reference": {
                                    "id": "6b618b0f-f377-41de-b3d7-dbf7c97e2b95",
                                    "label": "Creative Commons Attribution 4.0",
                                    "type": {
                                      "id": "2baab1e8-561f-4c15-84c3-571b811c52f6"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "5f0f3be2-2cd8-4911-999e-4cb1450d8277",
                                  "plan": {},
                                  "reference": {
                                    "id": "27b3c1d3-5c4d-4c2a-8692-2ea07f5d3cc9",
                                    "label": "International Iberian Nanotechnology Laboratory",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "7a1b8123-1ac9-4f39-9586-ee906df7a64c",
                                  "plan": {},
                                  "reference": {
                                    "id": "38d9d98e-6b59-4166-8618-2e22bb5136e4",
                                    "label": "University of Luxembourg",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a42e3c7b-76c8-40e6-ae35-b860679be270",
                                  "plan": {},
                                  "reference": {
                                    "id": "3bf6dea1-7fea-4607-ad3a-a5eac9258687",
                                    "label": "The National Centre of Scientific Research Demokritos",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "cc425a84-5bc2-4a28-8d45-8c1cb62874bd",
                                  "plan": {},
                                  "reference": {
                                    "id": "64f02d2e-807b-42b4-9546-bed4d29458fe",
                                    "label": "RINA CONSULTING SPA",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2fe82633-216b-4dcc-9d6d-82254397d6d7",
                                  "plan": {},
                                  "reference": {
                                    "id": "b81a0fb3-0125-40e4-ad5b-8ec0a9b109ba",
                                    "label": "UNIVERSITA DEGLI STUDI DI GENOVA",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "3cf7d736-a7e7-4f71-ad9a-ff8e5705c055",
                                  "plan": {},
                                  "reference": {
                                    "id": "3127c601-60d5-4a49-b59f-bd1faa1a4d74",
                                    "label": "SOLVIONIC SA",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "f333c9e6-0a8b-4594-ad05-6e4d662de47b",
                                  "plan": {},
                                  "reference": {
                                    "id": "ea7e344e-2002-45f8-bb54-8c928a18580e",
                                    "label": "Centre National de la Recherche Scientifique (CNRS), Paris",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "5283e296-4c89-4661-861b-c08e20d818c7",
                                  "plan": {},
                                  "reference": {
                                    "id": "84db0436-616f-4396-beeb-2d73a65f15a4",
                                    "label": "Tim Böhnert (orcid:0000-0002-2659-1481)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "e647920b-a67a-4fa8-9714-e9be4811f6b8",
                                  "plan": {},
                                  "reference": {
                                    "id": "4e739fbc-178e-4c9f-b74e-8a02cca86c6d",
                                    "label": "Fatemeh Shahbazi Farahani (orcid:0000-0002-5115-6879)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "94dcbf9f-5cbf-4e15-91e6-adaec31cfe60",
                                  "plan": {},
                                  "reference": {
                                    "id": "53eca1c6-04b1-421d-b7e0-4ac4a96b0460",
                                    "label": "Maria de Lourdes Gonzalez-Juarez (orcid:0000-0003-1046-4901)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "8387f75d-b08d-49aa-ba28-6ca4a4cdcc7d",
                                  "plan": {},
                                  "reference": {
                                    "id": "790bcaaa-5bd0-4692-822c-0a8eeeb725e8",
                                    "label": "Andrea Messina (orcid:0000-0002-6166-8210)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "964ced96-4ae9-4313-be29-2483485c4f8f",
                                  "plan": {},
                                  "reference": {
                                    "id": "7b5fb028-e81c-4000-b87f-f2b90909e59f",
                                    "label": "Carlos Marques (orcid:0000-0002-7429-0165)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "e8eef708-583c-4080-8026-dce3b0891405",
                                  "plan": {},
                                  "reference": {
                                    "id": "db3499f0-a425-4285-a614-bba991a633a5",
                                    "label": "Cinzia Leone (orcid:0000-0003-1734-6633)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a7f1cc1a-1e44-40fe-b633-e9193be9b8ed",
                                  "plan": {},
                                  "reference": {
                                    "id": "6cdab8d8-ece7-4d4d-9381-576afb7cd612",
                                    "label": "Nikolaos Ntallis (orcid:0000-0002-4444-6819)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "d2bcd4ca-d91d-45cf-a7e0-b14144915690",
                                  "plan": {},
                                  "reference": {
                                    "id": "dfe935db-134e-4872-a1b3-974612351dc6",
                                    "label": "Cláudia Coelho (orcid:0000-0003-0730-5651)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "09cca2aa-077b-4711-8078-1f6e245072fa",
                                  "plan": {},
                                  "reference": {
                                    "id": "3658bcff-389f-4807-a77f-9f59fcab5715",
                                    "label": "Chiara Lambruschini (orcid:0000-0003-1447-2650)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "6e125b93-491a-4be0-ad9f-9a67dee205ff",
                                  "plan": {},
                                  "reference": {
                                    "id": "147c2835-7138-4880-a350-c247c1a571b5",
                                    "label": "ANDREA VIAN (orcid:0000-0003-0629-0427)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "17b33aab-f90e-4df1-a21c-e7f882e8ba2d",
                                  "plan": {},
                                  "reference": {
                                    "id": "d84266ea-53cb-434e-930e-16074d36fd05",
                                    "label": "Michael Casale (orcid:0000-0002-5427-9401)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a7271355-b5ff-4dbe-8e77-ba38d4248717",
                                  "plan": {},
                                  "reference": {
                                    "id": "b72e2ff0-8bc2-46a4-aba6-f55668ba6216",
                                    "label": "Diego Colombara (orcid:0000-0002-8306-0994)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "efdbbf93-601b-4a61-bd49-9c30894fb70b",
                                  "plan": {},
                                  "reference": {
                                    "id": "9b90eaad-dea7-4f52-8dcd-42f741d8523e",
                                    "label": "Alexander Omelyanchik (orcid:0000-0003-3876-8261)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "0c13fa6d-89f7-4415-bdad-97ee9f747bce",
                                  "plan": {},
                                  "reference": {
                                    "id": "cd9871b9-e377-413d-a7fd-fa0a0b890bc7",
                                    "label": "Davide Peddis (orcid:0000-0003-0810-8860)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "98ef78f6-382c-4668-a2e4-e16e7f564a07",
                                  "plan": {},
                                  "reference": {
                                    "id": "750b28b3-4d4b-41f9-8bbe-62f01f429feb",
                                    "label": "Nicoleta Nicoara (orcid:0000-0002-0909-4635)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "65920cc2-798b-47ba-a729-2ab382ce13b4",
                                  "plan": {},
                                  "reference": {
                                    "id": "a177a1f2-5485-430d-951c-4e6d1fea0fc9",
                                    "label": "Davide Ressegotti (orcid:0009-0007-1517-0781)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "db894657-404f-4e08-9efc-2d500d935189",
                                  "plan": {},
                                  "reference": {
                                    "id": "91d3b286-fe85-4b18-9165-c252c3adec40",
                                    "label": "Phillip Dale (orcid:0000-0003-4821-8669)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2c8f6fd0-e53b-4cc7-b47f-f475522fa7f1",
                                  "plan": {},
                                  "reference": {
                                    "id": "f26d9766-7243-4960-bea2-d2243229d7e2",
                                    "label": "Serena De Negri (orcid:0000-0002-5345-8694)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "3cc86564-dd3a-428d-96e5-75c3b2613b54",
                                  "plan": {},
                                  "reference": {
                                    "id": "4d5b5582-ea54-4dd7-b351-e6bde6e5f53c",
                                    "label": "Pedro Anacleto (orcid:0000-0001-5404-0866)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "6d64c1b8-c025-40cf-b898-b4d9e2f5fb83",
                                  "plan": {},
                                  "reference": {
                                    "id": "683ab544-1503-489c-9315-84124a169b29",
                                    "label": "sawssen slimani (orcid:0000-0002-5830-1864)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "93ff8d51-2665-4d30-9183-8a3884bb4257",
                                  "plan": {},
                                  "reference": {
                                    "id": "0fac044a-351d-4e5a-8608-252278cb3f7d",
                                    "label": "Valerio Pagliarella (orcid:0009-0004-7081-1068)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2a711aae-465a-4a43-b5f7-16f8a7843d3a",
                                  "plan": {},
                                  "reference": {
                                    "id": "10905576-53c4-47cd-85c0-e9bad8fb73c1",
                                    "label": "Marianna Vasilakaki (orcid:0000-0003-1832-7549)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "4bf9c896-ddfd-415f-839d-67b1360592c3",
                                  "plan": {},
                                  "reference": {
                                    "id": "0b1284d1-5ddb-47aa-86fb-aff7ceaf9503",
                                    "label": "Alessandro Dell'Uomo (orcid:0009-0009-3907-3075)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "c2dcebbc-822c-44e8-9a12-69c9720064ca",
                                  "plan": {},
                                  "reference": {
                                    "id": "bd1f8b38-2c58-4961-9876-5c78cac21cf3",
                                    "label": "Marco Piccinni (orcid:0000-0003-4504-8000)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "8971adfb-f570-47b6-8e7f-0da62c7a2c1e",
                                  "plan": {},
                                  "reference": {
                                    "id": "20124b31-84c4-409d-aef7-61a06e40d61f",
                                    "label": "Sascha Sadewasser (orcid:0000-0001-8384-6025)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "1502bd7b-6acd-4d6e-bbee-841485c1de6a",
                                  "plan": {},
                                  "reference": {
                                    "id": "f5517adc-183b-4d99-9a5c-02bd46669279",
                                    "label": "Jean-Pierre Miranda Murillo (orcid:0000-0002-8404-0776)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "e397b355-8be1-4fd3-a664-1294af66f4da",
                                  "plan": {},
                                  "reference": {
                                    "id": "28e9b46a-650c-4924-b2c9-2dd946dad0e5",
                                    "label": "Rita Bencivenga (orcid:0000-0002-6298-141X)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "f8be15a2-cd2c-424b-a2fc-1c6ab19dcd25",
                                  "plan": {},
                                  "reference": {
                                    "id": "b64060ce-e194-4168-89ec-0608cde03e45",
                                    "label": "Giorgia Paniati (orcid:0000-0002-8454-7761)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2a41a990-cfc5-4d7b-83ae-374435c9bf1c",
                                  "plan": {},
                                  "reference": {
                                    "id": "51eadc53-bfa9-4c44-ae20-d3180dbc2a33",
                                    "label": "Charlotte Hurel (orcid:0000-0001-6187-6354)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "bcebf66c-1012-4820-be8c-c872aaaba657",
                                  "plan": {},
                                  "reference": {
                                    "id": "d231070f-94d1-4a45-88d0-26d6008c4879",
                                    "label": "Andrea Toscano (orcid:0000-0002-2604-0436)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "ce15c374-a9e4-4818-923d-a492309c3547",
                                  "plan": {},
                                  "reference": {
                                    "id": "e4d5039a-a6a2-474f-8a7c-75536d56f288",
                                    "label": "Kalliopi Trohidou (orcid:0000-0002-6921-5419)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "79c7fb66-2872-4677-a1f4-7b4b0fc5848e",
                                  "plan": {},
                                  "reference": {
                                    "id": "db5895e2-4f84-4f29-a9eb-0f37f23c26b1",
                                    "label": "Simona Delsante (orcid:0000-0002-7403-3425)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "c78a61ef-c2cb-4afc-8fdf-c2cf6226a7d6",
                                  "plan": {},
                                  "reference": {
                                    "id": "026a05bd-5627-40dd-8590-63465ed77ef6",
                                    "label": "Christian Rossi (orcid:0000-0002-2274-791X)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "06d9e916-bda4-4e67-96b7-d35a21a3d14c",
                                  "plan": {},
                                  "reference": {
                                    "id": "44160725-bfe5-4f77-9826-a950a163813e",
                                    "label": "Annalisa Barla (orcid:0000-0002-3436-035X)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a8417b21-d189-438c-9ae5-c36cd879bdc6",
                                  "plan": {},
                                  "reference": {
                                    "id": "e4e7e674-b0c3-4400-a750-d39cc71f608e",
                                    "label": "REusable MAsk Patterning (corda_____he::101046909)",
                                    "type": {
                                      "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "6bb0596a-653a-4d52-94e8-8b2fec073d90",
                                  "plan": {},
                                  "reference": {
                                    "id": "c9b3f64c-c49c-4526-b80f-ef32ce775dfa",
                                    "label": "European Commission||EC",
                                    "type": {
                                      "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                    }
                                  },
                                  "isActive": 1
                                }
                              ],
                              "descriptions": [
                                {
                                  "id": "7fa629e5-492a-4a12-9ed1-04cabb378738",
                                  "label": "D3.1 Demonstration of REMAP’s fundamental hypothesis",
                                  "status": {
                                    "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                    "name": "Finalized",
                                    "internalStatus": 1
                                  },
                                  "planDescriptionTemplate": {},
                                  "descriptionTemplate": {
                                    "groupId": "c105616e-3e8c-4375-8294-b7302a538fe5"
                                  },
                                  "plan": {}
                                }
                              ]
                            },
                            {
                              "id": "4e933ef9-dd62-4ec7-89d0-cd7bfef636c7",
                              "label": "Wastewater antibiotic resistance genes",
                              "version": 2,
                              "description": "This study aims to characterize the microbiome and resistome signatures in WW from 15 Latvian municipalities, leveraging short-read metagenomic data from untreated waste water samples. To address wider contexts, we explored the impacts on the microbiomes of the WW samples and industrial city environments",
                              "updatedAt": "2024-10-08T06:11:20Z",
                              "finalizedAt": "2024-10-08T09:11:20Z",
                              "status": {
                                "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                "name": "Finalized",
                                "internalStatus": 1,
                                "definition": {
                                  "availableActions": [
                                    0,
                                    1,
                                    2
                                  ]
                                }
                              },
                              "groupId": "482ee9ef-fbe7-4d21-b2d0-0cc2a5ad7eba",
                              "accessType": 0,
                              "planUsers": [
                                {
                                  "id": "7c7c3076-f787-4d22-9c7b-be41a0eb4852",
                                  "plan": {
                                    "id": "4e933ef9-dd62-4ec7-89d0-cd7bfef636c7"
                                  },
                                  "user": {
                                    "id": "c1aa30c3-4b2a-4926-8fd1-46a56fc86cdc"
                                  },
                                  "role": 0
                                }
                              ],
                              "planReferences": [
                                {
                                  "id": "72668825-cbef-4f2f-b70d-d39f0d9591ae",
                                  "plan": {},
                                  "reference": {
                                    "id": "9b088b70-8993-4f5b-8b5d-f66bf66d3cc0",
                                    "label": "Latvian Biomedical Research and Study Centre",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "4ac6351e-fbad-4ace-bfc2-e4564ded9b41",
                                  "plan": {},
                                  "reference": {
                                    "id": "a5dffaae-ff8e-4254-a4d3-6c3b2f3e4317",
                                    "label": "Edgars Liepa (orcid:0009-0003-6872-2819)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "973d977b-feb2-4285-ad76-d2647e9f43fd",
                                  "plan": {},
                                  "reference": {
                                    "id": "c581288e-00ec-4bf8-9bb6-680da4c22f4a",
                                    "label": "The Recovery and Resilience Facility ( 5.2.1.1.i.0/2/24/I/CFLA/001)",
                                    "type": {
                                      "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "de8e44cf-f902-411c-9adb-50f55261ab43",
                                  "plan": {},
                                  "reference": {
                                    "id": "18321eb1-e1da-40ef-896b-5176d4508930",
                                    "label": "Central Finance and Contracting Agency",
                                    "type": {
                                      "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                    }
                                  },
                                  "isActive": 1
                                }
                              ],
                              "descriptions": [
                                {
                                  "id": "782857cd-03c1-4803-9e9c-c04dbece9948",
                                  "label": "Wastewater metagenome",
                                  "status": {
                                    "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                    "name": "Finalized",
                                    "internalStatus": 1
                                  },
                                  "planDescriptionTemplate": {},
                                  "descriptionTemplate": {
                                    "groupId": "cc785a69-2d90-46c4-baba-3bedbfc4d997"
                                  },
                                  "plan": {}
                                }
                              ]
                            },
                            {
                              "id": "8a206e47-d21c-40ff-993c-71a59a61183d",
                              "label": "Characterization of Cancer-Associated Fibroblasts in Gastroenteropancreatic Neuroendocrine  Tumors using spatial transcriptomics",
                              "version": 0,
                              "description": "<b>This DMP characterizes the data management plan for study devoted to characterization of cancer-associated fibroblasts in gastroenteropancreatic neuroendocrine  tumors (GEP-NETs) using spatial transcriptomics</b><div><b><i>Study abstract:</i>&#160;</b><span>Cancer-associated fibroblasts (CAF) are an essential part of the tumor microenvironment </span><span>(TME). CAFs promote tumor progression by extracellular matrix (ECM) remodelling, metabolic effects,&#160;</span><span>secretion of soluble factors, and immunogenic interactions. Although crosstalk between CAFs and tumor cells&#160;</span><span>has been identified in gastroenteropancreatic neuroendocrine tumors (GEP-NETs) the exact role and molecular&#160;</span><span>mechanisms of CAFs in GEP-NET tumorigenesis still remain poorly understood.&#160;&#160;</span><span>In this project, we aim to&#160;</span><span>identify and characterize specific subspecies of CAFs found in GEP-NET tumors using single-cell RNA-seq and spatial transcriptomics approaches in order to profile the impact of CAFs&#160;</span><span>on tumor cell transcriptome, and assess the crosstalk between tumor and stroma.</span></div>",
                              "updatedAt": "2024-10-08T04:34:21Z",
                              "finalizedAt": "2024-10-08T07:34:21Z",
                              "status": {
                                "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                "name": "Finalized",
                                "internalStatus": 1,
                                "definition": {
                                  "availableActions": [
                                    0,
                                    1,
                                    2
                                  ]
                                }
                              },
                              "groupId": "d99a56e6-26c3-47fb-ad67-46b798596b1f",
                              "accessType": 0,
                              "planUsers": [
                                {
                                  "id": "007bf9c7-40b9-489e-8129-2f3c9d6d8d3d",
                                  "plan": {
                                    "id": "8a206e47-d21c-40ff-993c-71a59a61183d"
                                  },
                                  "user": {
                                    "id": "43ad0b43-0f86-4692-8af5-13cd28fd4680"
                                  },
                                  "role": 0
                                }
                              ],
                              "planReferences": [
                                {
                                  "id": "ebe482e5-00f5-46a5-8f1e-d8cba351cbed",
                                  "plan": {},
                                  "reference": {
                                    "id": "9b088b70-8993-4f5b-8b5d-f66bf66d3cc0",
                                    "label": "Latvian Biomedical Research and Study Centre",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "9d6ae716-0fe7-4f7a-aa7b-8ab914ec9737",
                                  "plan": {},
                                  "reference": {
                                    "id": "037b85b7-6031-4e39-947a-3df5bc3858ca",
                                    "label": "Ilona Mandrika (orcid:0000-0003-3822-9540)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "a6eddc3f-bb71-4bf0-8977-9161d308f943",
                                  "plan": {},
                                  "reference": {
                                    "id": "e4e487de-c185-4924-9231-02c0a5118472",
                                    "label": "Helvijs Niedra (orcid:0000-0003-4396-4021)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "2137f1e4-de96-46e5-81ad-e527ce7385d0",
                                  "plan": {},
                                  "reference": {
                                    "id": "67c63aaa-9078-49dc-ad4f-ea1c2321169b",
                                    "label": "Vita Rovite (orcid:0000-0001-9368-5210)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "9ba22627-37aa-4b45-b88f-695def3ee037",
                                  "plan": {},
                                  "reference": {
                                    "id": "da8d92ff-ee49-47e3-89b4-6bfce207c603",
                                    "label": "Consolidation of the Latvian Institute of Organic Synthesis and the Latvian Biomedical Research and Study Centre (Nr.5.2.1.1.i.0/2/24/I/CFLA/001 )",
                                    "type": {
                                      "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "f7ad26ee-0062-4b67-8501-ce4d73b81d02",
                                  "plan": {},
                                  "reference": {
                                    "id": "18321eb1-e1da-40ef-896b-5176d4508930",
                                    "label": "Central Finance and Contracting Agency",
                                    "type": {
                                      "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                    }
                                  },
                                  "isActive": 1
                                }
                              ],
                              "descriptions": [
                                {
                                  "id": "b6aee234-5c40-47e7-b08c-b1cd255aa480",
                                  "label": "LCS FARP",
                                  "status": {
                                    "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                    "name": "Finalized",
                                    "internalStatus": 1
                                  },
                                  "planDescriptionTemplate": {},
                                  "descriptionTemplate": {
                                    "groupId": "cc785a69-2d90-46c4-baba-3bedbfc4d997"
                                  },
                                  "plan": {}
                                }
                              ]
                            },
                            {
                              "id": "2032a1b8-7415-4857-a45d-de7805e4dc75",
                              "label": "Dynamics of black holes and scalar fields",
                              "version": 0,
                              "description": "Data produced with the Einstein Toolkit, for evolutions of black holes and bosonic fields.<br>",
                              "updatedAt": "2024-10-05T12:31:46Z",
                              "finalizedAt": "2024-10-05T15:31:46Z",
                              "status": {
                                "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                "name": "Finalized",
                                "internalStatus": 1,
                                "definition": {
                                  "availableActions": [
                                    0,
                                    1,
                                    2
                                  ]
                                }
                              },
                              "groupId": "3361ce7f-42a1-418a-ad2a-79ce464df729",
                              "accessType": 0,
                              "planUsers": [
                                {
                                  "id": "8aa24dfd-a136-49da-bb4b-aabc4521e9e5",
                                  "plan": {
                                    "id": "2032a1b8-7415-4857-a45d-de7805e4dc75"
                                  },
                                  "user": {
                                    "id": "27db9835-ce4f-4856-92d5-617e9128ff3b"
                                  },
                                  "role": 0
                                }
                              ],
                              "planReferences": [
                                {
                                  "id": "b31db445-0997-43f8-95c8-1322adeffa0c",
                                  "plan": {},
                                  "reference": {
                                    "id": "0ae9229b-9304-411a-8bb5-549a2a421dc4",
                                    "label": "Creative Commons Attribution-NonCommercial 4.0",
                                    "type": {
                                      "id": "2baab1e8-561f-4c15-84c3-571b811c52f6"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "be8e920f-bb09-4963-88c0-d3c5b96f8864",
                                  "plan": {},
                                  "reference": {
                                    "id": "b6e61798-e2b3-4c58-8e02-b257ad59534c",
                                    "label": "Universidade de Aveiro Centro de Investigação e Desenvolvimento em Matemática e Aplicações",
                                    "type": {
                                      "id": "7eeffb98-58fb-4921-82ec-e27f32f8e738"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "cae2fe03-0c63-4807-90e5-723f83511e9e",
                                  "plan": {},
                                  "reference": {
                                    "id": "8bb150e2-52e1-4986-8659-1f5663cd20e0",
                                    "label": "Miguel Zilhao (orcid:0000-0002-7089-5570)",
                                    "type": {
                                      "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "3affbc86-1ad2-46e0-adf4-6414f29c1e44",
                                  "plan": {},
                                  "reference": {
                                    "id": "2787b08c-0c6d-453b-a5cb-ee3b82a9c508",
                                    "label": "Gravitational waves, black holes, and fundamental physics (2022.04560.PTDC)",
                                    "type": {
                                      "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                    }
                                  },
                                  "isActive": 1
                                },
                                {
                                  "id": "03231391-3171-47d0-8a56-831d9b7fc998",
                                  "plan": {},
                                  "reference": {
                                    "id": "cfd04c06-4260-4706-8bde-202f3bd7577d",
                                    "label": "Fundação para a Ciência e a Tecnologia, I.P.||FCT",
                                    "type": {
                                      "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                    }
                                  },
                                  "isActive": 1
                                }
                              ],
                              "descriptions": [
                                {
                                  "id": "9949012c-b541-44a3-8ffd-c84bdc29276f",
                                  "label": "EinsteinToolkit data",
                                  "status": {
                                    "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                    "name": "Finalized",
                                    "internalStatus": 1
                                  },
                                  "planDescriptionTemplate": {},
                                  "descriptionTemplate": {
                                    "groupId": "7ae1c3d3-91a9-4315-9aec-b0f173010f2a"
                                  },
                                  "plan": {}
                                }
                              ]
                            }
                          ],
                          "count": 196
                        }
                        """;
    }

    public static final class Description {

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";

        public static final String endpoint_public_query =
                """
                        This endpoint is used to fetch all the available public published descriptions.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_public_query_request_body =
                """
                        For public descriptions we used the same query parameters as descriptions queries
                        """;

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
                            <li><b>statusIds:</b>
                            This is a list and determines which records we want to include in the response, based on if they have one of the specific status ids.. <br/>If empty, every record is included.
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
                            "project": {
                                "fields": [
                                    "id",
                                    "label",
                                    "description",
                                    "status.id",
                                    "status.name",
                                    "status.internalStatus",
                                    "status.definition.availableActions",
                                    "authorizationFlags.EditDescription",
                                    "authorizationFlags.DeleteDescription",
                                    "authorizationFlags.FinalizeDescription",
                                    "authorizationFlags.AnnotateDescription",
                                    "statusAuthorizationFlags.Edit",
                                    "planDescriptionTemplate.id",
                                    "planDescriptionTemplate.sectionId",
                                    "planDescriptionTemplate.isActive",
                                    "properties.fieldSets.comment",
                                    "properties.fieldSets.items.ordinal",
                                    "properties.fieldSets.items.fields.textValue",
                                    "properties.fieldSets.items.fields.textListValue",
                                    "properties.fieldSets.items.fields.dateValue",
                                    "properties.fieldSets.items.fields.booleanValue",
                                    "properties.fieldSets.items.fields.externalIdentifier.identifier",
                                    "properties.fieldSets.items.fields.externalIdentifier.type",
                                    "properties.fieldSets.items.fields.references.id",
                                    "properties.fieldSets.items.fields.references.label",
                                    "properties.fieldSets.items.fields.references.type.id",
                                    "properties.fieldSets.items.fields.references.type.name",
                                    "properties.fieldSets.items.fields.references.reference",
                                    "properties.fieldSets.items.fields.references.isActive",
                                    "properties.fieldSets.items.fields.tags.id",
                                    "properties.fieldSets.items.fields.tags.label",
                                    "properties.fieldSets.items.fields.tags.isActive",
                                    "properties.fieldSets.items.fields.tags.hash",
                                    "descriptionTags.id",
                                    "descriptionTags.tag.label",
                                    "descriptionTags.isActive",
                                    "descriptionReferences.data.fieldId",
                                    "descriptionReferences.data.ordinal",
                                    "descriptionReferences.reference.id",
                                    "descriptionReferences.reference.label",
                                    "descriptionReferences.reference.type.id",
                                    "descriptionReferences.reference.reference",
                                    "descriptionReferences.reference.source",
                                    "descriptionReferences.reference.sourceType",
                                    "descriptionReferences.isActive",
                                    "createdAt",
                                    "hash",
                                    "isActive",
                                    "belongsToCurrentTenant",
                                    "availableStatuses.id",
                                    "availableStatuses.name",
                                    "availableStatuses.internalStatus",
                                    "availableStatuses.action",
                                    "plan.id",
                                    "plan.label",
                                    "plan.status.id",
                                    "plan.status.name",
                                    "plan.status.internalStatus",
                                    "plan.isActive",
                                    "plan.authorizationFlags.EditPlan",
                                    "plan.blueprint.id",
                                    "plan.blueprint.isActive",
                                    "plan.blueprint.definition",
                                    "plan.blueprint.definition.sections.id",
                                    "plan.blueprint.definition.sections.label",
                                    "plan.blueprint.definition.sections.ordinal",
                                    "plan.blueprint.definition.sections.hasTemplates",
                                    "plan.blueprint.definition.sections.descriptionTemplates.descriptionTemplate.groupId",
                                    "plan.blueprint.definition.sections.descriptionTemplates.maxMultiplicity",
                                    "plan.blueprint.definition.sections.prefillingSourcesEnabled",
                                    "plan.blueprint.definition.sections.prefillingSources.id",
                                    "plan.planDescriptionTemplates.id",
                                    "plan.planDescriptionTemplates.sectionId",
                                    "plan.planDescriptionTemplates.descriptionTemplateGroupId",
                                    "plan.planDescriptionTemplates.isActive",
                                    "plan.planDescriptionTemplates.currentDescriptionTemplate.id",
                                    "plan.planDescriptionTemplates.currentDescriptionTemplate.label",
                                    "plan.planDescriptionTemplates.currentDescriptionTemplate.version",
                                    "plan.descriptions.id",
                                    "plan.descriptions.isActive",
                                    "plan.descriptions.planDescriptionTemplate.id",
                                    "plan.descriptions.planDescriptionTemplate.descriptionTemplateGroupId",
                                    "plan.descriptions.planDescriptionTemplate.sectionId",
                                    "plan.descriptions.planDescriptionTemplate.isActive",
                                    "plan.planUsers.id",
                                    "plan.planUsers.sectionId",
                                    "plan.planUsers.user.id",
                                    "plan.planUsers.user.name",
                                    "plan.planUsers.role",
                                    "plan.planUsers.isActive"
                                ]
                            },
                            "metadata": {
                                "countAll": true
                            },
                            "page": {
                                "offset": 0,
                                "size": 5
                            },
                            "isActive": [
                                1
                            ],
                            "order": {
                                "items": [
                                    "-updatedAt"
                                ]
                            }
                        }
                        """;
        public static final String endpoint_public_query_request_body_example =
                """
                        {
                             "project": {
                                 "fields": [
                                     "id",
                                     "tenantId",
                                     "label",
                                     "isActive",
                                     "status.id",
                                     "status.name",
                                     "status.internalStatus",
                                     "status.definition.availableActions",
                                     "status.definition.statusColor",
                                     "status.definition.matIconName",
                                     "status.definition.storageFile.id",
                                     "updatedAt",
                                     "belongsToCurrentTenant",
                                     "finalizedAt",
                                     "authorizationFlags.EditDescription",
                                     "authorizationFlags.DeleteDescription",
                                     "authorizationFlags.InvitePlanUsers",
                                     "authorizationFlags.CloneDescription",
                                     "descriptionTemplate.id",
                                     "descriptionTemplate.label",
                                     "descriptionTemplate.groupId",
                                     "plan.id",
                                     "plan.label",
                                     "plan.status.id",
                                     "plan.status.name",
                                     "plan.status.internalStatus",
                                     "plan.accessType",
                                     "plan.finalizedAt",
                                     "plan.blueprint.id",
                                     "plan.blueprint.label",
                                     "plan.blueprint.definition.sections.id",
                                     "plan.blueprint.definition.sections.label",
                                     "plan.blueprint.definition.sections.hasTemplates",
                                     "plan.planUsers.id",
                                     "plan.planUsers.user.id",
                                     "plan.planUsers.role",
                                     "plan.planUsers.isActive",
                                     "plan.planReferences.id",
                                     "plan.planReferences.reference.id",
                                     "plan.planReferences.reference.label",
                                     "plan.planReferences.reference.type.id",
                                     "plan.planReferences.reference.reference",
                                     "plan.planReferences.isActive",
                                     "planDescriptionTemplate.id",
                                     "planDescriptionTemplate.plan.id",
                                     "planDescriptionTemplate.descriptionTemplateGroupId",
                                     "planDescriptionTemplate.sectionId"
                                 ]
                             },
                             "metadata": {
                                 "countAll": true
                             },
                             "page": {
                                 "offset": 0,
                                 "size": 5
                             },
                             "isActive": [
                                 1
                             ],
                             "order": {
                                 "items": [
                                     "-updatedAt"
                                 ]
                             }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "e2f7a248-9956-476f-8086-6e6f5782bbe5",
                              "label": "Horizon Europe",
                              "status": {
                                "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    0
                                  ],
                                  "storageFile": {},
                                  "statusColor": ""
                                }
                              },
                              "updatedAt": "2025-04-02T07:30:00.351629Z",
                              "isActive": 1,
                              "planDescriptionTemplate": {
                                "id": "e735e425-8bb5-4402-9bb1-22069c432660",
                                "plan": {
                                  "id": "7d6bc285-7ea1-48b3-b02b-8a78ab5edb38"
                                },
                                "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                "descriptionTemplateGroupId": "c105616e-3e8c-4375-8294-b7302a538fe5"
                              },
                              "descriptionTemplate": {
                                "id": "05fcea9a-0435-4c67-a84d-df152aad868e",
                                "label": "Horizon Europe",
                                "groupId": "c105616e-3e8c-4375-8294-b7302a538fe5"
                              },
                              "authorizationFlags": [
                                "DeleteDescription",
                                "EditDescription",
                                "CloneDescription",
                                "InvitePlanUsers"
                              ],
                              "plan": {
                                "id": "7d6bc285-7ea1-48b3-b02b-8a78ab5edb38",
                                "label": "test",
                                "status": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft",
                                  "internalStatus": 0
                                },
                                "blueprint": {
                                  "id": "86635178-36a6-484f-9057-a934e4eeecd5",
                                  "label": "Dmp Default Blueprint",
                                  "definition": {
                                    "sections": [
                                      {
                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                        "label": "Main Info",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                        "label": "Funding",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                        "label": "License",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "label": "Templates",
                                        "hasTemplates": true
                                      }
                                    ]
                                  }
                                },
                                "planUsers": [
                                  {
                                    "id": "7770de72-5d62-4ee8-b67d-df17f197ccc5",
                                    "plan": {},
                                    "user": {
                                      "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                    },
                                    "role": 0,
                                    "isActive": 1
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "159e0728-b739-49e1-9154-9b8b111fc662",
                              "label": "Test Case Ethics",
                              "status": {
                                "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    0
                                  ],
                                  "storageFile": {},
                                  "statusColor": ""
                                }
                              },
                              "updatedAt": "2025-03-26T14:39:17.011868Z",
                              "isActive": 1,
                              "planDescriptionTemplate": {
                                "id": "8edb1a4b-9b2d-4cf3-8f92-416c9172af2c",
                                "plan": {
                                  "id": "835f920e-efb1-498a-95af-52703d34665b"
                                },
                                "sectionId": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                "descriptionTemplateGroupId": "dfb12676-44a5-4b99-b50b-d41fa61bbfdd"
                              },
                              "descriptionTemplate": {
                                "id": "d10f0207-1705-4034-9348-129e00e4bf67",
                                "label": "Test Case Ethics",
                                "groupId": "dfb12676-44a5-4b99-b50b-d41fa61bbfdd"
                              },
                              "authorizationFlags": [
                                "DeleteDescription",
                                "EditDescription",
                                "CloneDescription",
                                "InvitePlanUsers"
                              ],
                              "plan": {
                                "id": "835f920e-efb1-498a-95af-52703d34665b",
                                "label": "Test Case Plan",
                                "status": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft",
                                  "internalStatus": 0
                                },
                                "accessType": 0,
                                "blueprint": {
                                  "id": "e6da321c-e1ca-4c26-8f30-daedeb6ae702",
                                  "label": "Test Case",
                                  "definition": {
                                    "sections": [
                                      {
                                        "id": "c55987a1-abff-8b00-fe44-bde18127ff66",
                                        "label": "Main Info",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                        "label": "Privacy",
                                        "hasTemplates": true
                                      },
                                      {
                                        "id": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                        "label": "Ethics",
                                        "hasTemplates": true
                                      },
                                      {
                                        "id": "61aae446-08c7-def0-cee5-f9e386677de6",
                                        "label": "Datasets",
                                        "hasTemplates": true
                                      }
                                    ]
                                  }
                                },
                                "planUsers": [
                                  {
                                    "id": "c44f5369-9b0c-4b59-93ca-6435aa5fb67a",
                                    "plan": {},
                                    "user": {
                                      "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                    },
                                    "role": 0,
                                    "isActive": 1
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "f1163b6b-7446-483d-9f5a-b755439e0ce0",
                              "label": "Test case Privacy Template",
                              "status": {
                                "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    0
                                  ],
                                  "storageFile": {},
                                  "statusColor": ""
                                }
                              },
                              "updatedAt": "2025-03-26T14:38:57.753194Z",
                              "isActive": 1,
                              "planDescriptionTemplate": {
                                "id": "b9d17602-3c04-454b-9da1-ceeb87944a3d",
                                "plan": {
                                  "id": "835f920e-efb1-498a-95af-52703d34665b"
                                },
                                "sectionId": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                "descriptionTemplateGroupId": "87f605df-e877-4520-8e0d-9fd503ffc87e"
                              },
                              "descriptionTemplate": {
                                "id": "0de97e49-2708-471c-a6d8-eee9962dbf78",
                                "label": "Test case Privacy Template",
                                "groupId": "87f605df-e877-4520-8e0d-9fd503ffc87e"
                              },
                              "authorizationFlags": [
                                "DeleteDescription",
                                "EditDescription",
                                "CloneDescription",
                                "InvitePlanUsers"
                              ],
                              "plan": {
                                "id": "835f920e-efb1-498a-95af-52703d34665b",
                                "label": "Test Case Plan",
                                "status": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft",
                                  "internalStatus": 0
                                },
                                "accessType": 0,
                                "blueprint": {
                                  "id": "e6da321c-e1ca-4c26-8f30-daedeb6ae702",
                                  "label": "Test Case",
                                  "definition": {
                                    "sections": [
                                      {
                                        "id": "c55987a1-abff-8b00-fe44-bde18127ff66",
                                        "label": "Main Info",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                        "label": "Privacy",
                                        "hasTemplates": true
                                      },
                                      {
                                        "id": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                        "label": "Ethics",
                                        "hasTemplates": true
                                      },
                                      {
                                        "id": "61aae446-08c7-def0-cee5-f9e386677de6",
                                        "label": "Datasets",
                                        "hasTemplates": true
                                      }
                                    ]
                                  }
                                },
                                "planUsers": [
                                  {
                                    "id": "c44f5369-9b0c-4b59-93ca-6435aa5fb67a",
                                    "plan": {},
                                    "user": {
                                      "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                    },
                                    "role": 0,
                                    "isActive": 1
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "3206acd7-8099-4c70-9477-2f2effded209",
                              "label": "EvoRoads Template - V.3",
                              "status": {
                                "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    0
                                  ],
                                  "storageFile": {},
                                  "statusColor": ""
                                }
                              },
                              "updatedAt": "2025-03-20T08:37:46.838294Z",
                              "isActive": 1,
                              "planDescriptionTemplate": {
                                "id": "7389c637-b5b1-43a7-9da0-cd47e5cfb55b",
                                "plan": {
                                  "id": "13052cb2-896c-42f8-8f60-1580059519c9"
                                },
                                "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                "descriptionTemplateGroupId": "cbdb7b33-5eb3-4d94-9cc5-bbc4b72b3037"
                              },
                              "descriptionTemplate": {
                                "id": "11a92e42-197b-4212-b5bd-f3eb31d03546",
                                "label": "EvoRoads Template v.5",
                                "groupId": "cbdb7b33-5eb3-4d94-9cc5-bbc4b72b3037"
                              },
                              "authorizationFlags": [
                                "DeleteDescription",
                                "EditDescription",
                                "CloneDescription",
                                "InvitePlanUsers"
                              ],
                              "plan": {
                                "id": "13052cb2-896c-42f8-8f60-1580059519c9",
                                "label": "Dmp Default Blueprint Plan (from test-argos)",
                                "status": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft",
                                  "internalStatus": 0
                                },
                                "blueprint": {
                                  "id": "6db28659-36e5-4d5a-bf5e-222822d31768",
                                  "label": "Dmp_Default_Blueprint.xml",
                                  "definition": {
                                    "sections": [
                                      {
                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                        "label": "Main Info",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                        "label": "Funding",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                        "label": "License",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "label": "Templates",
                                        "hasTemplates": true
                                      }
                                    ]
                                  }
                                },
                                "planUsers": [
                                  {
                                    "id": "9eb13f99-c9c9-49e7-9a9e-82460794809d",
                                    "plan": {},
                                    "user": {
                                      "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                    },
                                    "role": 0,
                                    "isActive": 1
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "e6c1e91d-76f4-4786-8aa9-a076a95012de",
                              "label": "CHIST-ERA Data Management",
                              "status": {
                                "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                "name": "Draft",
                                "internalStatus": 0,
                                "definition": {
                                  "availableActions": [
                                    0
                                  ],
                                  "storageFile": {},
                                  "statusColor": ""
                                }
                              },
                              "updatedAt": "2025-03-14T12:30:28.931916Z",
                              "isActive": 1,
                              "finalizedAt": "2025-03-14T11:51:46.808045Z",
                              "planDescriptionTemplate": {
                                "id": "53162c98-4138-44fd-9e87-ab8786dec21b",
                                "plan": {
                                  "id": "646cb52e-9e11-431b-a7e9-9d5024c020b1"
                                },
                                "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                "descriptionTemplateGroupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a"
                              },
                              "descriptionTemplate": {
                                "id": "f8474cfb-ef80-4bac-a865-7120afa1931a",
                                "label": "CHIST-ERA Data Management",
                                "groupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a"
                              },
                              "authorizationFlags": [
                                "DeleteDescription",
                                "EditDescription",
                                "CloneDescription",
                                "InvitePlanUsers"
                              ],
                              "plan": {
                                "id": "646cb52e-9e11-431b-a7e9-9d5024c020b1",
                                "label": "My Plan",
                                "status": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft",
                                  "internalStatus": 0
                                },
                                "finalizedAt": "2025-03-14T12:08:14.574464Z",
                                "accessType": 0,
                                "blueprint": {
                                  "id": "58317184-8130-4ea9-8cbc-a0f2148da92c",
                                  "label": "Test_Dmp_Default_Blueprint.xml",
                                  "definition": {
                                    "sections": [
                                      {
                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                        "label": "Main Info",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                        "label": "Funding",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                        "label": "License",
                                        "hasTemplates": false
                                      },
                                      {
                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "label": "Templates",
                                        "hasTemplates": true
                                      }
                                    ]
                                  }
                                },
                                "planReferences": [
                                  {
                                    "id": "fd43f3e6-a980-402d-82c8-13f2fa4a4da7",
                                    "plan": {},
                                    "reference": {
                                      "id": "cc42747c-e778-44dd-9a82-3187167a3c12",
                                      "label": " Ana  Rivera ",
                                      "type": {
                                        "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                      },
                                      "reference": "0000-0002-7457-4547"
                                    },
                                    "isActive": 1
                                  },
                                  {
                                    "id": "e5cdf025-1fbe-4e42-ab49-f5ab80f50ec5",
                                    "plan": {},
                                    "reference": {
                                      "id": "4ed45781-b627-41bf-8f54-f53ccc6841bf",
                                      "label": " Fundação de Amparo à Pesquisa do Estado de São Paulo",
                                      "type": {
                                        "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                      },
                                      "reference": "https://ror.org/02ddkpn78"
                                    },
                                    "isActive": 1
                                  },
                                  {
                                    "id": "232c8890-83ba-44f3-bfe7-15cbe79562ed",
                                    "plan": {},
                                    "reference": {
                                      "id": "4c049eaa-813c-4b1d-a699-7e1c3a571d31",
                                      "label": "  SuperGraCOF - Superconducting Graphene/Covalent Organic frameworks bilayers studied by large scale atomistic methods (2023.10530.CPCA.A2)",
                                      "type": {
                                        "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                      },
                                      "reference": "2023.10530.CPCA.A2"
                                    },
                                    "isActive": 1
                                  }
                                ],
                                "planUsers": [
                                  {
                                    "id": "326da564-97f1-4d52-a3c3-216beba48c81",
                                    "plan": {},
                                    "user": {
                                      "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                    },
                                    "role": 0,
                                    "isActive": 1
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            }
                          ],
                          "count": 5194
                        }
                        """;
        public static final String endpoint_public_query_response_example =
                """
                        {
                            "items": [
                                {
                                    "id": "e2f7a248-9956-476f-8086-6e6f5782bbe5",
                                    "label": "Horizon Europe",
                                    "status": {
                                        "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                        "name": "Draft",
                                        "internalStatus": 0,
                                        "definition": {
                                            "availableActions": [
                                                0
                                            ],
                                            "storageFile": {},
                                            "statusColor": ""
                                        }
                                    },
                                    "updatedAt": "2025-04-02T07:30:00.351629Z",
                                    "isActive": 1,
                                    "planDescriptionTemplate": {
                                        "id": "e735e425-8bb5-4402-9bb1-22069c432660",
                                        "plan": {
                                            "id": "7d6bc285-7ea1-48b3-b02b-8a78ab5edb38"
                                        },
                                        "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "descriptionTemplateGroupId": "c105616e-3e8c-4375-8294-b7302a538fe5"
                                    },
                                    "descriptionTemplate": {
                                        "id": "05fcea9a-0435-4c67-a84d-df152aad868e",
                                        "label": "Horizon Europe",
                                        "groupId": "c105616e-3e8c-4375-8294-b7302a538fe5"
                                    },
                                    "authorizationFlags": [
                                        "DeleteDescription",
                                        "EditDescription",
                                        "CloneDescription",
                                        "InvitePlanUsers"
                                    ],
                                    "plan": {
                                        "id": "7d6bc285-7ea1-48b3-b02b-8a78ab5edb38",
                                        "label": "test",
                                        "status": {
                                            "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                            "name": "Draft",
                                            "internalStatus": 0
                                        },
                                        "blueprint": {
                                            "id": "86635178-36a6-484f-9057-a934e4eeecd5",
                                            "label": "Dmp Default Blueprint",
                                            "definition": {
                                                "sections": [
                                                    {
                                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                        "label": "Main Info",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                        "label": "Funding",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                        "label": "License",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                        "label": "Templates",
                                                        "hasTemplates": true
                                                    }
                                                ]
                                            }
                                        },
                                        "planUsers": [
                                            {
                                                "id": "7770de72-5d62-4ee8-b67d-df17f197ccc5",
                                                "plan": {},
                                                "user": {
                                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                                },
                                                "role": 0,
                                                "isActive": 1
                                            }
                                        ]
                                    },
                                    "belongsToCurrentTenant": true
                                },
                                {
                                    "id": "159e0728-b739-49e1-9154-9b8b111fc662",
                                    "label": "Test Case Ethics",
                                    "status": {
                                        "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                        "name": "Draft",
                                        "internalStatus": 0,
                                        "definition": {
                                            "availableActions": [
                                                0
                                            ],
                                            "storageFile": {},
                                            "statusColor": ""
                                        }
                                    },
                                    "updatedAt": "2025-03-26T14:39:17.011868Z",
                                    "isActive": 1,
                                    "planDescriptionTemplate": {
                                        "id": "8edb1a4b-9b2d-4cf3-8f92-416c9172af2c",
                                        "plan": {
                                            "id": "835f920e-efb1-498a-95af-52703d34665b"
                                        },
                                        "sectionId": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                        "descriptionTemplateGroupId": "dfb12676-44a5-4b99-b50b-d41fa61bbfdd"
                                    },
                                    "descriptionTemplate": {
                                        "id": "d10f0207-1705-4034-9348-129e00e4bf67",
                                        "label": "Test Case Ethics",
                                        "groupId": "dfb12676-44a5-4b99-b50b-d41fa61bbfdd"
                                    },
                                    "authorizationFlags": [
                                        "DeleteDescription",
                                        "EditDescription",
                                        "CloneDescription",
                                        "InvitePlanUsers"
                                    ],
                                    "plan": {
                                        "id": "835f920e-efb1-498a-95af-52703d34665b",
                                        "label": "Test Case Plan",
                                        "status": {
                                            "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                            "name": "Draft",
                                            "internalStatus": 0
                                        },
                                        "accessType": 0,
                                        "blueprint": {
                                            "id": "e6da321c-e1ca-4c26-8f30-daedeb6ae702",
                                            "label": "Test Case",
                                            "definition": {
                                                "sections": [
                                                    {
                                                        "id": "c55987a1-abff-8b00-fe44-bde18127ff66",
                                                        "label": "Main Info",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                                        "label": "Privacy",
                                                        "hasTemplates": true
                                                    },
                                                    {
                                                        "id": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                                        "label": "Ethics",
                                                        "hasTemplates": true
                                                    },
                                                    {
                                                        "id": "61aae446-08c7-def0-cee5-f9e386677de6",
                                                        "label": "Datasets",
                                                        "hasTemplates": true
                                                    }
                                                ]
                                            }
                                        },
                                        "planUsers": [
                                            {
                                                "id": "c44f5369-9b0c-4b59-93ca-6435aa5fb67a",
                                                "plan": {},
                                                "user": {
                                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                                },
                                                "role": 0,
                                                "isActive": 1
                                            }
                                        ]
                                    },
                                    "belongsToCurrentTenant": true
                                },
                                {
                                    "id": "f1163b6b-7446-483d-9f5a-b755439e0ce0",
                                    "label": "Test case Privacy Template",
                                    "status": {
                                        "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                        "name": "Draft",
                                        "internalStatus": 0,
                                        "definition": {
                                            "availableActions": [
                                                0
                                            ],
                                            "storageFile": {},
                                            "statusColor": ""
                                        }
                                    },
                                    "updatedAt": "2025-03-26T14:38:57.753194Z",
                                    "isActive": 1,
                                    "planDescriptionTemplate": {
                                        "id": "b9d17602-3c04-454b-9da1-ceeb87944a3d",
                                        "plan": {
                                            "id": "835f920e-efb1-498a-95af-52703d34665b"
                                        },
                                        "sectionId": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                        "descriptionTemplateGroupId": "87f605df-e877-4520-8e0d-9fd503ffc87e"
                                    },
                                    "descriptionTemplate": {
                                        "id": "0de97e49-2708-471c-a6d8-eee9962dbf78",
                                        "label": "Test case Privacy Template",
                                        "groupId": "87f605df-e877-4520-8e0d-9fd503ffc87e"
                                    },
                                    "authorizationFlags": [
                                        "DeleteDescription",
                                        "EditDescription",
                                        "CloneDescription",
                                        "InvitePlanUsers"
                                    ],
                                    "plan": {
                                        "id": "835f920e-efb1-498a-95af-52703d34665b",
                                        "label": "Test Case Plan",
                                        "status": {
                                            "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                            "name": "Draft",
                                            "internalStatus": 0
                                        },
                                        "accessType": 0,
                                        "blueprint": {
                                            "id": "e6da321c-e1ca-4c26-8f30-daedeb6ae702",
                                            "label": "Test Case",
                                            "definition": {
                                                "sections": [
                                                    {
                                                        "id": "c55987a1-abff-8b00-fe44-bde18127ff66",
                                                        "label": "Main Info",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "47f8e637-398a-9005-02c5-bb5eea686bbc",
                                                        "label": "Privacy",
                                                        "hasTemplates": true
                                                    },
                                                    {
                                                        "id": "6dfa3c8d-1519-6698-5196-88cd6608dfa1",
                                                        "label": "Ethics",
                                                        "hasTemplates": true
                                                    },
                                                    {
                                                        "id": "61aae446-08c7-def0-cee5-f9e386677de6",
                                                        "label": "Datasets",
                                                        "hasTemplates": true
                                                    }
                                                ]
                                            }
                                        },
                                        "planUsers": [
                                            {
                                                "id": "c44f5369-9b0c-4b59-93ca-6435aa5fb67a",
                                                "plan": {},
                                                "user": {
                                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                                },
                                                "role": 0,
                                                "isActive": 1
                                            }
                                        ]
                                    },
                                    "belongsToCurrentTenant": true
                                },
                                {
                                    "id": "3206acd7-8099-4c70-9477-2f2effded209",
                                    "label": "EvoRoads Template - V.3",
                                    "status": {
                                        "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                        "name": "Draft",
                                        "internalStatus": 0,
                                        "definition": {
                                            "availableActions": [
                                                0
                                            ],
                                            "storageFile": {},
                                            "statusColor": ""
                                        }
                                    },
                                    "updatedAt": "2025-03-20T08:37:46.838294Z",
                                    "isActive": 1,
                                    "planDescriptionTemplate": {
                                        "id": "7389c637-b5b1-43a7-9da0-cd47e5cfb55b",
                                        "plan": {
                                            "id": "13052cb2-896c-42f8-8f60-1580059519c9"
                                        },
                                        "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "descriptionTemplateGroupId": "cbdb7b33-5eb3-4d94-9cc5-bbc4b72b3037"
                                    },
                                    "descriptionTemplate": {
                                        "id": "11a92e42-197b-4212-b5bd-f3eb31d03546",
                                        "label": "EvoRoads Template v.5",
                                        "groupId": "cbdb7b33-5eb3-4d94-9cc5-bbc4b72b3037"
                                    },
                                    "authorizationFlags": [
                                        "DeleteDescription",
                                        "EditDescription",
                                        "CloneDescription",
                                        "InvitePlanUsers"
                                    ],
                                    "plan": {
                                        "id": "13052cb2-896c-42f8-8f60-1580059519c9",
                                        "label": "Dmp Default Blueprint Plan (from test-argos)",
                                        "status": {
                                            "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                            "name": "Draft",
                                            "internalStatus": 0
                                        },
                                        "blueprint": {
                                            "id": "6db28659-36e5-4d5a-bf5e-222822d31768",
                                            "label": "Dmp_Default_Blueprint.xml",
                                            "definition": {
                                                "sections": [
                                                    {
                                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                        "label": "Main Info",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                        "label": "Funding",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                        "label": "License",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                        "label": "Templates",
                                                        "hasTemplates": true
                                                    }
                                                ]
                                            }
                                        },
                                        "planUsers": [
                                            {
                                                "id": "9eb13f99-c9c9-49e7-9a9e-82460794809d",
                                                "plan": {},
                                                "user": {
                                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                                },
                                                "role": 0,
                                                "isActive": 1
                                            }
                                        ]
                                    },
                                    "belongsToCurrentTenant": true
                                },
                                {
                                    "id": "e6c1e91d-76f4-4786-8aa9-a076a95012de",
                                    "label": "CHIST-ERA Data Management",
                                    "status": {
                                        "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                        "name": "Draft",
                                        "internalStatus": 0,
                                        "definition": {
                                            "availableActions": [
                                                0
                                            ],
                                            "storageFile": {},
                                            "statusColor": ""
                                        }
                                    },
                                    "updatedAt": "2025-03-14T12:30:28.931916Z",
                                    "isActive": 1,
                                    "finalizedAt": "2025-03-14T11:51:46.808045Z",
                                    "planDescriptionTemplate": {
                                        "id": "53162c98-4138-44fd-9e87-ab8786dec21b",
                                        "plan": {
                                            "id": "646cb52e-9e11-431b-a7e9-9d5024c020b1"
                                        },
                                        "sectionId": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                        "descriptionTemplateGroupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a"
                                    },
                                    "descriptionTemplate": {
                                        "id": "f8474cfb-ef80-4bac-a865-7120afa1931a",
                                        "label": "CHIST-ERA Data Management",
                                        "groupId": "c8ef1ecc-f0a6-4f06-a62d-2769968c3d0a"
                                    },
                                    "authorizationFlags": [
                                        "DeleteDescription",
                                        "EditDescription",
                                        "CloneDescription",
                                        "InvitePlanUsers"
                                    ],
                                    "plan": {
                                        "id": "646cb52e-9e11-431b-a7e9-9d5024c020b1",
                                        "label": "My Plan",
                                        "status": {
                                            "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                            "name": "Draft",
                                            "internalStatus": 0
                                        },
                                        "finalizedAt": "2025-03-14T12:08:14.574464Z",
                                        "accessType": 0,
                                        "blueprint": {
                                            "id": "58317184-8130-4ea9-8cbc-a0f2148da92c",
                                            "label": "Test_Dmp_Default_Blueprint.xml",
                                            "definition": {
                                                "sections": [
                                                    {
                                                        "id": "f94e50e0-cb97-4c65-8b88-e5db6badd41d",
                                                        "label": "Main Info",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "3c2608e5-9320-4d94-9ed7-1eab9500d84b",
                                                        "label": "Funding",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "2a77e1f6-9989-4aeb-acd9-48e911a92abd",
                                                        "label": "License",
                                                        "hasTemplates": false
                                                    },
                                                    {
                                                        "id": "0db7845b-0e7c-41df-8d91-cbca97995fd5",
                                                        "label": "Templates",
                                                        "hasTemplates": true
                                                    }
                                                ]
                                            }
                                        },
                                        "planReferences": [
                                            {
                                                "id": "fd43f3e6-a980-402d-82c8-13f2fa4a4da7",
                                                "plan": {},
                                                "reference": {
                                                    "id": "cc42747c-e778-44dd-9a82-3187167a3c12",
                                                    "label": " Ana  Rivera ",
                                                    "type": {
                                                        "id": "5a2112e7-ea99-4cfe-98a1-68665e26726e"
                                                    },
                                                    "reference": "0000-0002-7457-4547"
                                                },
                                                "isActive": 1
                                            },
                                            {
                                                "id": "e5cdf025-1fbe-4e42-ab49-f5ab80f50ec5",
                                                "plan": {},
                                                "reference": {
                                                    "id": "4ed45781-b627-41bf-8f54-f53ccc6841bf",
                                                    "label": " Fundação de Amparo à Pesquisa do Estado de São Paulo",
                                                    "type": {
                                                        "id": "538928bb-c7c6-452e-b66d-08e539f5f082"
                                                    },
                                                    "reference": "https://ror.org/02ddkpn78"
                                                },
                                                "isActive": 1
                                            },
                                            {
                                                "id": "232c8890-83ba-44f3-bfe7-15cbe79562ed",
                                                "plan": {},
                                                "reference": {
                                                    "id": "4c049eaa-813c-4b1d-a699-7e1c3a571d31",
                                                    "label": "  SuperGraCOF - Superconducting Graphene/Covalent Organic frameworks bilayers studied by large scale atomistic methods (2023.10530.CPCA.A2)",
                                                    "type": {
                                                        "id": "5b9c284f-f041-4995-96cc-fad7ad13289c"
                                                    },
                                                    "reference": "2023.10530.CPCA.A2"
                                                },
                                                "isActive": 1
                                            }
                                        ],
                                        "planUsers": [
                                            {
                                                "id": "326da564-97f1-4d52-a3c3-216beba48c81",
                                                "plan": {},
                                                "user": {
                                                    "id": "c1a25d94-ff7e-4a6c-9a0e-35c6e5352cd2"
                                                },
                                                "role": 0,
                                                "isActive": 1
                                            }
                                        ]
                                    },
                                    "belongsToCurrentTenant": true
                                }
                            ],
                            "count": 5194
                        }
                        """;
    }

    public static final class DescriptionTemplate {

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";

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
                          "project": {
                            "fields": [
                              "id",
                              "label",
                              "code",
                              "status",
                              "description",
                              "language",
                              "status",
                              "authorizationFlags.EditDescriptionTemplate",
                              "authorizationFlags.DeleteDescriptionTemplate",
                              "authorizationFlags.CloneDescriptionTemplate",
                              "authorizationFlags.CreateNewVersionDescriptionTemplate",
                              "authorizationFlags.ImportDescriptionTemplate",
                              "authorizationFlags.ExportDescriptionTemplate",
                              "type.id",
                              "type.name",
                              "definition.pluginConfigurations.pluginCode",
                              "definition.pluginConfigurations.pluginType",
                              "definition.pluginConfigurations.fields.code",
                              "definition.pluginConfigurations.fields.textValue",
                              "definition.pluginConfigurations.fields.fileValue.id",
                              "definition.pluginConfigurations.fields.fileValue.extension",
                              "definition.pluginConfigurations.fields.fileValue.mimeType",
                              "definition.pluginConfigurations.fields.fileValue.fullName",
                              "definition.pluginConfigurations.fields.fileValue.name",
                              "definition.pages.id",
                              "definition.pages.ordinal",
                              "definition.pages.title",
                              "definition.pages.sections.id",
                              "definition.pages.sections.ordinal",
                              "definition.pages.sections.title",
                              "definition.pages.sections.description",
                              "definition.pages.sections.ordinal",
                              "definition.pages.sections.sections",
                              "definition.pages.sections.fieldSets.id",
                              "definition.pages.sections.fieldSets.ordinal",
                              "definition.pages.sections.fieldSets.title",
                              "definition.pages.sections.fieldSets.description",
                              "definition.pages.sections.fieldSets.extendedDescription",
                              "definition.pages.sections.fieldSets.additionalInformation",
                              "definition.pages.sections.fieldSets.hasCommentField",
                              "definition.pages.sections.fieldSets.hasMultiplicity",
                              "definition.pages.sections.fieldSets.multiplicity.min",
                              "definition.pages.sections.fieldSets.multiplicity.max",
                              "definition.pages.sections.fieldSets.multiplicity.placeholder",
                              "definition.pages.sections.fieldSets.multiplicity.tableView",
                              "definition.pages.sections.fieldSets.fields.id",
                              "definition.pages.sections.fieldSets.fields.ordinal",
                              "definition.pages.sections.fieldSets.fields.semantics",
                              "definition.pages.sections.fieldSets.fields.defaultValue.textValue",
                              "definition.pages.sections.fieldSets.fields.defaultValue.dateValue",
                              "definition.pages.sections.fieldSets.fields.defaultValue.booleanValue",
                              "definition.pages.sections.fieldSets.fields.includeInExport",
                              "definition.pages.sections.fieldSets.fields.validations",
                              "definition.pages.sections.fieldSets.fields.visibilityRules.target",
                              "definition.pages.sections.fieldSets.fields.visibilityRules.textValue",
                              "definition.pages.sections.fieldSets.fields.visibilityRules.dateValue",
                              "definition.pages.sections.fieldSets.fields.visibilityRules.booleanValue",
                              "definition.pages.sections.fieldSets.fields.data.label",
                              "definition.pages.sections.fieldSets.fields.data.fieldType",
                              "definition.pages.sections.fieldSets.fields.data.multipleSelect",
                              "definition.pages.sections.fieldSets.fields.data.options.label",
                              "definition.pages.sections.fieldSets.fields.data.options.value",
                              "definition.pages.sections.fieldSets.fields.data.multipleSelect",
                              "definition.pages.sections.fieldSets.fields.data.type",
                              "definition.pages.sections.fieldSets.fields.data.referenceType.id",
                              "definition.pages.sections.fieldSets.fields.data.referenceType.name",
                              "definition.pages.sections.fieldSets.fields.data.maxFileSizeInMB",
                              "definition.pages.sections.fieldSets.fields.data.types.label",
                              "definition.pages.sections.fieldSets.fields.data.types.value",
                              "users.descriptionTemplate.id",
                              "users.user.id",
                              "users.user.name",
                              "users.role",
                              "users.isActive",
                              "createdAt",
                              "hash",
                              "isActive",
                              "belongsToCurrentTenant"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
                              "-createdAt"
                            ]
                          },
                          "versionStatuses": [
                            0,
                            2
                          ],
                          "onlyCanEdit": true
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
                           "project": {
                             "fields": [
                               "id",
                               "name",
                               "code",
                               "status",
                               "updatedAt",
                               "createdAt",
                               "hash",
                               "belongsToCurrentTenant",
                               "isActive"
                             ]
                           },
                           "metadata": {
                             "countAll": true
                           },
                           "page": {
                             "offset": 0,
                             "size": 10
                           },
                           "isActive": [
                             1
                           ],
                           "order": {
                             "items": [
                               "-createdAt"
                             ]
                           }
                         }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                           "items": [
                             {
                               "id": "709a8400-10ca-11ee-be56-0242ac120002",
                               "code": "091dda38-10fc-4895-893b-ede3230374ef",
                               "name": "Dataset",
                               "createdAt": "2024-10-14T10:02:43.341299Z",
                               "updatedAt": "2024-10-14T10:02:43.341299Z",
                               "isActive": 1,
                               "status": 1,
                               "hash": "1728900163",
                               "belongsToCurrentTenant": true
                             },
                             {
                               "id": "3b15c046-a978-4b5a-9376-66525b7f1ac9",
                               "code": "74ee0e72-bb24-48fc-b910-3531e84338da",
                               "name": "Software",
                               "createdAt": "2024-10-14T10:02:43.341299Z",
                               "updatedAt": "2024-10-14T10:02:43.341299Z",
                               "isActive": 1,
                               "status": 1,
                               "hash": "1728900163",
                               "belongsToCurrentTenant": true
                             }
                           ],
                           "count": 2
                         }
                        """;

    }

    public static final class PlanBlueprint {

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";

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
                          "project": {
                            "fields": [
                              "id",
                              "label",
                              "description",
                              "code",
                              "status",
                              "definition.pluginConfigurations.pluginCode",
                              "definition.pluginConfigurations.pluginType",
                              "definition.pluginConfigurations.fields.code",
                              "definition.pluginConfigurations.fields.textValue",
                              "definition.pluginConfigurations.fields.fileValue.id",
                              "definition.pluginConfigurations.fields.fileValue.extension",
                              "definition.pluginConfigurations.fields.fileValue.mimeType",
                              "definition.pluginConfigurations.fields.fileValue.fullName",
                              "definition.pluginConfigurations.fields.fileValue.name",
                              "definition.sections.id",
                              "definition.sections.label",
                              "definition.sections.description",
                              "definition.sections.ordinal",
                              "definition.sections.hasTemplates",
                              "definition.sections.fields.id",
                              "definition.sections.fields.category",
                              "definition.sections.fields.label",
                              "definition.sections.fields.placeholder",
                              "definition.sections.fields.description",
                              "definition.sections.fields.required",
                              "definition.sections.fields.semantics",
                              "definition.sections.fields.ordinal",
                              "definition.sections.fields.systemFieldType",
                              "definition.sections.fields.dataType",
                              "definition.sections.fields.referenceType.id",
                              "definition.sections.fields.referenceType.name",
                              "definition.sections.fields.referenceType.code",
                              "definition.sections.fields.multipleSelect",
                              "definition.sections.fields.maxFileSizeInMB",
                              "definition.sections.fields.types",
                              "definition.sections.fields.types.label",
                              "definition.sections.fields.types.value",
                              "definition.sections.descriptionTemplates.descriptionTemplate.groupId",
                              "definition.sections.descriptionTemplates.descriptionTemplate.label",
                              "definition.sections.descriptionTemplates.minMultiplicity",
                              "definition.sections.descriptionTemplates.maxMultiplicity",
                              "definition.sections.canEditDescriptionTemplates",
                              "definition.sections.prefillingSources.id",
                              "definition.sections.prefillingSources.label",
                              "definition.sections.prefillingSources.code",
                              "definition.sections.prefillingSourcesEnabled",
                              "createdAt",
                              "hash",
                              "isActive",
                              "belongsToCurrentTenant"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
                              "-createdAt"
                            ]
                          },
                          "versionStatuses": [
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

        public static final String endpoint_field_set_example =
                "[\"id\", \"doi\", \"isActive\"]";

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

        public static final String endpoint_get_available_repos_example =
                "[\"repositoryId\", \"repositoryAuthorizationUrl\", \"repositoryRecordUrl\", \"repositoryClientId\", \"hasLogo\", \"redirectUri\", \"configurationFields\", \"userConfigurationFields\", \"pluginType\"]";

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
        public static final String endpoint_get_auth_methods =
                """
                        This endpoint is used to fetch available user authentication methods for a deposit repository.
                        """;

        public static final String endpoint_query_field_set_example =
                """
                             "depositType",
                             "repositoryId",
                             "repositoryAuthorizationUrl",
                             "repositoryRecordUrl"
                             "repositoryClientId",
                             "redirectUri",
                             "hasLogo",
                             "configurationFields",
                             "userConfigurationFields",
                             "pluginType"
                        """;

    }

    public static final class Tag {

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";

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

        public static final String endpoint_field_set_example =
                "[\"id\", \"label\", \"isActive\"]";


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
                          "project": {
                            "fields": [
                              "id",
                              "label",
                              "type.id",
                              "description",
                              "reference",
                              "abbreviation",
                              "source",
                              "sourceType",
                              "createdAt",
                              "updatedAt",
                              "definition.fields.code",
                              "definition.fields.dataType",
                              "definition.fields.value",
                              "hash",
                              "isActive",
                              "belongsToCurrentTenant"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
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
                                 "label",
                                 "type.id",
                                 "description",
                                 "reference",
                                 "abbreviation",
                                 "source",
                                 "sourceType",
                                 "createdAt",
                                 "updatedAt",
                                 "definition.fields.code",
                                 "definition.fields.dataType",
                                 "definition.fields.value",
                                 "hash",
                                 "isActive",
                                 "belongsToCurrentTenant"
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

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"isActive\"]";

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
                          "project": {
                            "fields": [
                              "id",
                              "name",
                              "code",
                              "definition.fields.code",
                              "definition.fields.label",
                              "definition.fields.description",
                              "definition.fields.dataType",
                              "definition.sources.type",
                              "definition.sources.key",
                              "definition.sources.label",
                              "definition.sources.ordinal",
                              "definition.sources.referenceTypeDependencies.id",
                              "definition.sources.referenceTypeDependencies.name",
                              "definition.sources.url",
                              "definition.sources.results.resultsArrayPath",
                              "definition.sources.results.fieldsMapping.code",
                              "definition.sources.results.fieldsMapping.responsePath",
                              "definition.sources.paginationPath",
                              "definition.sources.contentType",
                              "definition.sources.firstPage",
                              "definition.sources.httpMethod",
                              "definition.sources.requestBody",
                              "definition.sources.filterType",
                              "definition.sources.auth.enabled",
                              "definition.sources.auth.authUrl",
                              "definition.sources.auth.authMethod",
                              "definition.sources.auth.authTokenPath",
                              "definition.sources.auth.authRequestBody",
                              "definition.sources.auth.type",
                              "definition.sources.queries.name",
                              "definition.sources.queries.defaultValue",
                              "definition.sources.queries.cases.likePattern",
                              "definition.sources.queries.cases.separator",
                              "definition.sources.queries.cases.value",
                              "definition.sources.queries.cases.referenceType",
                              "definition.sources.queries.cases.referenceType.id",
                              "definition.sources.queries.cases.referenceType.name",
                              "definition.sources.queries.cases.referenceTypeSourceKey",
                              "definition.sources.headers.key",
                              "definition.sources.headers.value",
                              "definition.sources.items.options.code",
                              "definition.sources.items.options.value",
                              "createdAt",
                              "updatedAt",
                              "isActive",
                              "belongsToCurrentTenant"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
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

        public static final String endpoint_field_set_example =
                "[\"id\", \"target\", \"targetType\"]";

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

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"isActive\"]";

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

        public static final String endpoint_field_set_example =
                "[\"userId\", \"name\", \"language\"]";

        public static final String available_field_set=
                "Check the Schema for the available properties";

    }

    public static final class Tenant {

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"code\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available tenants.<br/>
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
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                          "project": {
                            "fields": [
                              "id",
                              "name",
                              "code",
                              "updatedAt",
                              "createdAt",
                              "hash",
                              "isActive"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
                              "-createdAt"
                            ]
                          }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "55ff1c50-4855-4b7d-bc75-a9721aa63a22",
                              "code": "tenant1",
                              "name": "Tenant 1",
                              "isActive": 1,
                              "createdAt": "2024-10-15T10:55:18.489064Z",
                              "updatedAt": "2024-10-15T10:55:18.489064Z",
                              "hash": "1728989718"
                            },
                            {
                              "id": "6c101c45-faee-418f-a812-0a7abbf119c3",
                              "code": "tenant2",
                              "name": "Tenant 2",
                              "isActive": 1,
                              "createdAt": "2024-10-15T10:25:50.523897Z",
                              "updatedAt": "2024-10-15T10:25:50.523897Z",
                              "hash": "1728987950"
                            }
                          ],
                          "count": 5
                        }
                        """;

    }

    public static final class PlanStatus {

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"internalStatus\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available plan statuses.<br/>
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
                        
                        ### <u>Plan Status specific query parameters:</u>
                        
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
                            <li><b>internalStatuses:</b>
                            This is a list and determines which records we want to include in the response, based on their internal status.
                            The status can be <i>Draft</i> or <i>Finalized</i> or <i>Null</i>. We add 0 or 1 to the list respectively or null.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project": {
                             "fields": [
                               "id",
                               "name",
                               "description",
                               "action",
                               "ordinal",
                               "internalStatus",
                               "definition",
                               "definition.authorization",
                               "definition.authorization.edit.roles",
                               "definition.authorization.edit.planRoles",
                               "definition.authorization.edit.allowAnonymous",
                               "definition.authorization.edit.allowAuthenticated",
                               "definition.availableActions",
                               "definition.matIconName",
                               "definition.storageFile.id",
                               "definition.storageFile.name",
                               "definition.storageFile.fullName",
                               "definition.storageFile.mimeType",
                               "definition.statusColor",
                               "updatedAt",
                               "createdAt",
                               "hash",
                               "isActive",
                               "belongsToCurrentTenant"
                             ]
                           },
                           "metadata": {
                             "countAll": true
                           },
                           "page": {
                             "offset": 0,
                             "size": 10
                           },
                           "isActive": [
                             1
                           ],
                           "order": {
                             "items": [
                               "-createdAt"
                             ]
                           }
                         }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                              "name": "Draft",
                              "ordinal": 0,
                              "createdAt": "2024-09-17T07:37:24.288404Z",
                              "updatedAt": "2025-04-01T08:16:21.256217Z",
                              "isActive": 1,
                              "internalStatus": 0,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  1,
                                  2
                                ],
                                "statusColor": "#ef0c92"
                              },
                              "hash": "1743495381",
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "313cce74-f44b-4a72-9cd8-a9c75fe03a7e",
                              "name": "Under Review",
                              "action": "Review",
                              "ordinal": 0,
                              "createdAt": "2024-09-17T07:36:58.677058Z",
                              "updatedAt": "2025-03-04T11:25:53.927120Z",
                              "isActive": 1,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  1
                                ],
                                "statusColor": "#00acc1"
                              },
                              "hash": "1741087553",
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "61fd91f5-c63a-45bc-aa7a-e1f00fbd8545",
                              "name": "Validated",
                              "action": "Validate",
                              "ordinal": 0,
                              "createdAt": "2024-09-17T07:36:38.386887Z",
                              "updatedAt": "2025-02-27T11:40:30.913140Z",
                              "isActive": 1,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0,
                                      3
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  1
                                ]
                              },
                              "hash": "1740656430",
                              "belongsToCurrentTenant": true
                            },
                            {
                              "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                              "name": "Finalized",
                              "action": "Finalize",
                              "ordinal": 0,
                              "createdAt": "2024-09-16T14:16:56.685177Z",
                              "updatedAt": "2025-04-09T13:38:38.895294Z",
                              "isActive": 1,
                              "internalStatus": 1,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0,
                                      1,
                                      2,
                                      3
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  0,
                                  1,
                                  2
                                ],
                                "statusColor": "#629929"
                              },
                              "hash": "1744205918",
                              "belongsToCurrentTenant": true
                            }
                          ],
                          "count": 4
                        }
                        """;

    }

    public static final class DescriptionStatus {

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"internalStatus\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available description statuses.<br/>
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
                        
                        ### <u>Plan Status specific query parameters:</u>
                        
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
                            <li><b>internalStatuses:</b>
                            This is a list and determines which records we want to include in the response, based on their internal status.
                            The status can be <i>Draft</i> or <i>Finalized</i> or <i>Null</i>. We add 0 or 1 to the list respectively or null.
                            <br/>If not present, every record is included.
                            </li>
                            <li><b>isActive:</b>
                            This is a list and determines which records we want to include in the response, based on if they are deleted or not.
                            This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].
                            <br/>If not present or if we pass [0,1], every record is included.
                            </li>
                        </ul>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                          "project": {
                            "fields": [
                              "id",
                              "name",
                              "description",
                              "action",
                              "ordinal",
                              "internalStatus",
                              "definition",
                              "definition.authorization",
                              "definition.authorization.edit.roles",
                              "definition.authorization.edit.planRoles",
                              "definition.authorization.edit.allowAnonymous",
                              "definition.authorization.edit.allowAuthenticated",
                              "definition.availableActions",
                              "definition.matIconName",
                              "definition.storageFile.id",
                              "definition.storageFile.name",
                              "definition.storageFile.fullName",
                              "definition.storageFile.extension",
                              "definition.storageFile.mimeType",
                              "definition.statusColor",
                              "updatedAt",
                              "createdAt",
                              "hash",
                              "isActive",
                              "belongsToCurrentTenant"
                            ]
                          },
                          "metadata": {
                            "countAll": true
                          },
                          "page": {
                            "offset": 0,
                            "size": 10
                          },
                          "isActive": [
                            1
                          ],
                          "order": {
                            "items": [
                              "-createdAt"
                            ]
                          }
                        }
                        """;

        public static final String endpoint_query_response_example =
                """
                        {
                          "items": [
                            {
                              "id": "60f5e529-7ed3-4be1-8754-ac8c7443f246",
                              "name": "Canceled",
                              "action": "Cancel",
                              "ordinal": 0,
                              "createdAt": "2024-09-17T15:37:42.256424Z",
                              "updatedAt": "2025-01-23T10:52:01.216120Z",
                              "isActive": 1,
                              "hash": "1737629521",
                              "belongsToCurrentTenant": true,
                              "internalStatus": 2,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                }
                              }
                            },
                            {
                              "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                              "name": "Finalized",
                              "action": "Finalize",
                              "ordinal": 0,
                              "createdAt": "2024-09-17T07:38:15.846399Z",
                              "updatedAt": "2025-05-16T14:17:34.703669Z",
                              "isActive": 1,
                              "hash": "1747405054",
                              "belongsToCurrentTenant": true,
                              "internalStatus": 1,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0,
                                      2,
                                      4,
                                      5
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  0
                                ],
                                "statusColor": "#a3e556"
                              }
                            },
                            {
                              "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                              "name": "Draft",
                              "ordinal": 0,
                              "createdAt": "2024-09-16T12:46:20.459486Z",
                              "updatedAt": "2025-05-16T14:17:59.393244Z",
                              "isActive": 1,
                              "hash": "1747405079",
                              "belongsToCurrentTenant": true,
                              "internalStatus": 0,
                              "definition": {
                                "authorization": {
                                  "edit": {
                                    "roles": [
                                      "Admin"
                                    ],
                                    "planRoles": [
                                      0,
                                      1,
                                      2,
                                      3,
                                      4,
                                      5,
                                      6
                                    ],
                                    "allowAuthenticated": false,
                                    "allowAnonymous": false
                                  }
                                },
                                "availableActions": [
                                  0
                                ],
                                "statusColor": ""
                              }
                            }
                          ],
                          "count": 3
                        }
                        """;

    }

    public static final class PlanWorkflow {

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"isActive\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available plan workflows.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "name",
                                 "description",
                                 "createdAt",
                                 "updatedAt",
                                 "isActive",
                                 "belongsToCurrentTenant",
                                 "definition",
                                 "definition.startingStatus.id",
                                 "definition.startingStatus.name",
                                 "definition.statusTransitions",
                                 "definition.statusTransitions.fromStatus.id",
                                 "definition.statusTransitions.fromStatus.name",
                                 "definition.statusTransitions.toStatus.id",
                                 "definition.statusTransitions.toStatus.name"
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
                          "items": [
                            {
                              "id": "44df0e24-7879-48cc-bbe0-cd8a2b618855",
                              "name": "default",
                              "createdAt": "2024-09-18T11:39:30.597400Z",
                              "updatedAt": "2024-11-11T14:35:01.773055Z",
                              "isActive": 1,
                              "definition": {
                                "startingStatus": {
                                  "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                  "name": "Draft"
                                },
                                "statusTransitions": [
                                  {
                                    "fromStatus": {
                                      "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                      "name": "Draft"
                                    },
                                    "toStatus": {
                                      "id": "313cce74-f44b-4a72-9cd8-a9c75fe03a7e",
                                      "name": "Under Review"
                                    }
                                  },
                                  {
                                    "fromStatus": {
                                      "id": "313cce74-f44b-4a72-9cd8-a9c75fe03a7e",
                                      "name": "Under Review"
                                    },
                                    "toStatus": {
                                      "id": "61fd91f5-c63a-45bc-aa7a-e1f00fbd8545",
                                      "name": "Validated"
                                    }
                                  },
                                  {
                                    "fromStatus": {
                                      "id": "61fd91f5-c63a-45bc-aa7a-e1f00fbd8545",
                                      "name": "Validated"
                                    },
                                    "toStatus": {
                                      "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                      "name": "Finalized"
                                    }
                                  },
                                  {
                                    "fromStatus": {
                                      "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                      "name": "Draft"
                                    },
                                    "toStatus": {
                                      "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                      "name": "Finalized"
                                    }
                                  },
                                  {
                                    "fromStatus": {
                                      "id": "f1a3da63-0bff-438f-8b46-1a81ca176115",
                                      "name": "Finalized"
                                    },
                                    "toStatus": {
                                      "id": "cb3ced76-9807-4829-82da-75777de1bc78",
                                      "name": "Draft"
                                    }
                                  }
                                ]
                              },
                              "belongsToCurrentTenant": true
                            }
                          ],
                          "count": 1
                        }
                        """;

    }

    public static final class DescriptionWorkflow {

        public static final String endpoint_field_set_example =
                "[\"id\", \"name\", \"isActive\"]";

        public static final String endpoint_query =
                """
                        This endpoint is used to fetch all the available plan workflows.<br/>
                        It also allows to restrict the results using a query object passed in the request body.<br/>
                        """;

        public static final String endpoint_query_request_body_example =
                """
                        {
                           "project":{
                              "fields":[
                                 "id",
                                 "name",
                                 "description",
                                 "createdAt",
                                 "updatedAt",
                                 "isActive",
                                 "belongsToCurrentTenant",
                                 "definition",
                                 "definition.startingStatus.id",
                                 "definition.startingStatus.name",
                                 "definition.statusTransitions",
                                 "definition.statusTransitions.fromStatus.id",
                                 "definition.statusTransitions.fromStatus.name",
                                 "definition.statusTransitions.toStatus.id",
                                 "definition.statusTransitions.toStatus.name"
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
                           "items": [
                             {
                               "id": "8651af83-8b24-4776-ae45-329031db9f5e",
                               "name": "default",
                               "createdAt": "2024-09-17T07:39:00.221933Z",
                               "updatedAt": "2024-11-19T14:03:29.761609Z",
                               "isActive": 1,
                               "definition": {
                                 "startingStatus": {
                                   "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                   "name": "Draft"
                                 },
                                 "statusTransitions": [
                                   {
                                     "fromStatus": {
                                       "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                       "name": "Draft"
                                     },
                                     "toStatus": {
                                       "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                       "name": "Finalized"
                                     }
                                   },
                                   {
                                     "fromStatus": {
                                       "id": "c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa",
                                       "name": "Finalized"
                                     },
                                     "toStatus": {
                                       "id": "978e6ff6-b5e9-4cee-86cb-bc7401ec4059",
                                       "name": "Draft"
                                     }
                                   }
                                 ]
                               },
                               "belongsToCurrentTenant": true
                             }
                           ],
                           "count": 1
                         }
                        """;

    }


}
