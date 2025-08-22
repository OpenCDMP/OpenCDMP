package org.opencdmp.query.lookup.swagger;

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

        public static final String like_description = "<li> If there is a like parameter present in the query, only the plan entities that include the contents of the parameter either in their labels or the descriptions will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String groupIds_description ="<li> This is a list and contains the group ids we want the plans to have. Every plan and all its versions, have the same groupId. <br/>If empty, every record is included. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String statusIds_description = "<li> This is a list and determines which records we want to include in the response, based on if they have one of the specific status ids.. <br/>If empty, every record is included.<li/>";

        public static final String versionStatuses_description ="<li> This is a list and determines which records we want to include in the response, based on their version status. The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively. <br/>If not present, every record is included. <li/>";

        public static final String accessTypes_description ="<li> This is a list and determines which records we want to include in the response, based on their access type. The access type can be <i>Public</i> or <i>Restricted</i>. We add 0 or 1 to the list respectively.<br/>If not present, every record is included. <li/>";

        public static final String versions_description = "<li> This is a list of version numbers. Only the records that match one of the specified versions will be included in the response. <br/>If empty or not present, version-based filtering is not applied.<li/>";

        public static final String planDescriptionTemplateSubQuery_description = "<li> Filters the response based on a subquery on related Plan Description Templates. Only records associated with templates matching the subquery conditions are included. <br/>If empty or not present, no filtering is applied based on templates.<li/>";

        public static final String planUserSubQuery_description = "<li> Filters the response based on a subquery on related Users. Only records associated with users matching the subquery conditions are included. <br/>If empty or not present, no user-based filtering is applied.<li/>";

        public static final String planBlueprintSubQuery_description = "<li> Filters the response based on a subquery on related Blueprints. Only records associated with blueprints matching the subquery conditions are included. <br/>If empty or not present, no filtering is applied based on blueprints.<li/>";

        public static final String planReferenceSubQuery_description = "<li> Filters the response based on a subquery on related Plan References. Only records that reference entities matching the subquery conditions are included. <br/>If empty or not present, no reference-based filtering is applied.<li/>";

        public static final String planStatusSubQuery_description = "<li> Filters the response based on a subquery on related Plan Status entities. Only records matching the status criteria are included. <br/>If empty or not present, status filtering is not applied.<li/>";

        public static final String tenantSubQuery_description = "<li> Filters the response based on a subquery on related Tenant entities. Only records associated with tenants matching the subquery conditions are included. <br/>If empty or not present, no tenant-based filtering is applied.<li/>";


    }

    public static final class PlanDescriptionTemplate {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String planIds_description = "<li> This is a list and contains the plan ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String descriptionTemplateGroupIds_description ="<li> This is a list and contains the group ids we want the plans to have. Every plan and all its versions, have the same groupId. <br/>If empty, every record is included. <li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String sectionIds_description = "<li>This is a list of section IDs. Only records linked to these sections will be included. <br/>If empty or not provided, no section-based filtering is applied.<li/>";

    }

    public static final class PlanReference {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String planIds_description = "<li> This is a list and contains the plan ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String referenceIds_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

    }

    public static final class PlanUser {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String planIds_description = "<li> This is a list and contains the plan ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String userIds_description = "<li> This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String sectionIds_description = "<li>This is a list of section IDs. Only records linked to these sections will be included. <br/>If empty or not provided, no section-based filtering is applied.<li/>";

        public static final String userRoles_description = "<li>This is a list of user roles. Only records matching these roles will be included. <br/>If empty or not provided, no role-based filtering is applied.<li/>";

    }

    public static final class Description {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the description entities that include the contents of the parameter either in their labels or the descriptions will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String statusIds_description = "<li>This is a list and determines which records we want to include in the response, based on if they have one of the specific status ids.. <br/>If empty, every record is included.<li/>";

        public static final String createdAfter_description = "<li>This is a date and determines which records we want to include in the response, based on their creation date. Specifically, only the records created after the given date are included.<br/>If not present, every record is included. <li/>";

        public static final String createdBefore_description = "<li>This is a date and determines which records we want to include in the response, based on their creation date.  Specifically, only the records created after the given date are included. <br/>If not present,every record is included.  <li/>.";

        public static final String finalizedAfter_description = "<li>This is a date and determines which records we want to include in the response, based on their finalization date. Specifically, only the records finalized after the given date are included. <br/>If not present, every record is included.  <li/>.";

        public static final String finalizedBefore_description = "<li>This is a date and determines which records we want to include in the response, based on their finalization date. Specifically, only the records finalized before the given date are included. <br/>If not present, every record is included.  <li/>.";

        public static final String planSubQuery_description = "<li> Filters the response based on a subquery on related Plan entities. These allow you to apply filters on those entities and return only the description records associated with them. <li/>";

        public static final String tenantSubQuery_description = "<li> Filters the response based on a subquery on related Tenant entities. These allow you to apply filters on those entities and return only the description records associated with them. <li/>";

        public static final String descriptionTemplateSubQuery_description = "<li> Filters the response based on a subquery on related Description Template entities. These allow you to apply filters on those entities and return only the description records associated with them. <li/>";

        public static final String descriptionReferenceSubQuery_description = "<li> Filters the response based on a subquery on related Description Reference entities. These allow you to apply filters on those entities and return only the description records associated with them.<li/>";

        public static final String descriptionTagSubQuery_description = "<li> Filters the response based on a subquery on related Description Tags. Only records tagged with tags matching the subquery will be included. These allow you to apply filters on those entities and return only the description records associated with them.<li/>";

        public static final String descriptionStatusSubQuery_description = "<li> Filters the response based on a subquery on Description Status entities. These allow you to apply filters on those entities and return only the description records associated with them. <li/>";

    }

    public static final class DescriptionTemplate {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the description template entities that include the contents of the parameter either in their labels or the descriptions will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String groupIds_description = "<li> This is a list and contains the group ids we want the templates to have. Every template and all its versions, have the same groupId. <br/>If empty, every record is included.";

        public static final String versions_description = "<li>This is a list of version numbers. Only templates matching these versions will be included. <br/>If empty or not provided, version-based filtering is not applied.<li/>";

        public static final String excludeGroupIds_description = "<li> This is a list and contains the group ids we want the templates not to have. Every template and all its versions, have the same groupId. <br/>If empty, every record is included.";

        public static final String typeIds_description = "<li> This is a list and contains the type ids we want the templates to have. Every template has a type designated by a type id. <br/>If empty, every record is included. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String statuses_description = "<li> This is a list and determines which records we want to include in the response, based on their status. The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively. <br/>If not present, every record is included.";

        public static final String versionStatuses_description = "<li> This is a list and determines which records we want to include in the response, based on their version status. The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively. <br/>If not present, every record is included.";

        public static final String languages_description = "<li>This is a list of language codes (e.g., 'en', 'fr'). Only templates in the specified languages will be included. <br/>If empty or not provided, language-based filtering is not applied.<li/>";

        public static final String onlyCanEdit_description = "<li> This is a boolean and determines whether to fetch only the templates the user can edit or every one. <br/>If not present, every record is included. <li/> ";

    }

    public static final class DescriptionTemplateType {

        public static final String like_description = "<li>  If there is a like parameter present in the query, only the description template type entities that include the contents of the parameter in their labels will be in the response.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String statuses_description = "<li>This is a list and determines which records we want to include in the response, based on their status. The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively. <br/>If not present, every record is included. <li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";
    }

    public static final class DescriptionTag {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String descriptionIds_description = "<li>This is a collection of description IDs. Only entities linked to these descriptions will be included. <br/>If empty or not provided, no description-based filtering is applied.<li/>";

        public static final String tagIds_description = "<li>This is a collection of tag IDs. Only entities associated with these tags will be included. <br/>If empty or not provided, no tag-based filtering is applied.<li/>";

    }

    public static final class DescriptionReference {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String descriptionIds_description = "<li>This is a collection of description IDs. Only entities linked to these descriptions will be included. <br/>If empty or not provided, no description-based filtering is applied.<li/>";

        public static final String referenceIds_description = "<li>This is a collection of reference IDs. Only entities linked to these references will be included. <br/>If empty or not provided, no tag-based filtering is applied.<li/>";

    }

    public static final class PlanBlueprint {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the blueprint entities that include the contents of the parameter either in their labels or the descriptions will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String groupIds_description ="<li> This is a list and contains the group ids we want the blueprints to have. Every plan and all its versions, have the same groupId. <br/>If empty, every record is included. <li/>";

        public static final String excludeGroupIds_description = "<li> This is a list and contains the group ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String statuses_description = "<li>This is a list and determines which records we want to include in the response, based on their status. The status can be <i>Draft</i> or <i>Finalized</i>. We add 0 or 1 to the list respectively. <br/>If not present, every record is included.<li/>";

        public static final String versionStatuses_description ="<li> This is a list and determines which records we want to include in the response, based on their version status. The status can be <i>Current</i>, <i>Previous</i> or <i>NotFinalized</i>. We add 0, 1 or 2 to the list respectively. <br/>If not present, every record is included. <li/>";


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

        public static final String types_description = "<li> This is a list and determines which records we want to include in the response, based on their type. The type can only be <i>DMP</i> currently. We add 0 to the list respectively. <br/>If not present, every record is included.<li/>";

        public static final String ids_description = "<li>This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included. <li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String dois_description = "<li>This is a list and determines which records we want to include in the response, based on their doi string. <br/>If not present, every record is included. </li>";

    }

    public static final class Deposit {


    }

    public static final class Tag {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the tag entities that include the contents of the parameter in their labels will be in the response. <li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included. <li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String createdByIds_description = "<li> This is a list and contains the ids of the users who's tags we want to exclude from the response. <br/>If empty, every record is included. <li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String tags_description = "<li> This is a list and determines which records we want to include in the response, based on their tag string. <br/>If not present, every record is included. <li/>";

        public static final String excludeTags_description = "<li> This is a list and determines which records we want to exclude from the response, based on their tag string. <br/>If not present, no record is excluded. <li/>";

    }

    public static final class Reference {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their labels, descriptions or the references will be in the response. <li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included. <li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String sourceTypes_description = "<li>This is a list and determines which records we want to include in the response, based on their resource type. The resource type can only be <i>Internal</i> or <i>External</i>. We add 0 or 1 to the list respectively. <br/>If not present, every record is included. <li/>";

        public static final String typeIds_description = "<li> This is the type id of the references we want in the response. <br/>If empty, every record is included. <li/>";

    }

    public static final class ReferenceType {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their names or their codes will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludedIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String codes_description = "<li> This is a list and determines which records we want to include in the response, based on their code. <br/>If not present, every record is included.<li/>";

    }

    public static final class Lock {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the locks locking the provided target id will be in the response. <li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String targetIds_description = "<li> This is a list and contains the ids of the locked targets of the locks we want to include in the response. <br/>If empty, every record is included. <li/>";

        public static final String userIds_description = "<li> This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String targetTypes_description = "<li> This is a list and determines which records we want to include in the response, based on their target type. The target type can be <i>Plan</i>, <i>Description</i>, <i>PlanBlueprint</i> or <i>DescriptionTemplate</i>. We add 0, 1, 2 or 3 to the list respectively. <br/>If not present, every record is included. <li/>";


    }

    public static final class User {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the reference entities that include the contents of the parameter either in their labels, descriptions or the references will be in the response.</li>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String emails_description = " <li> This is a list and determines which records we want to include in the response, based on their emails. <br/>If not present, every record is included. <li/>";

        public static final String excludeIds_description = " <li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String userRoleSubQuery_description = "<li>This is a subquery for filtering based on user roles. Only users with roles matching the subquery will be included. <br/>If not present or empty, no role-based filtering is applied.<li/>";

        public static final String tenantUserSubQuery_description = "<li>This is a subquery for filtering users based on their associations with tenants. Only users matching the tenant conditions will be included. <br/>If not present or empty, no tenant-based filtering is applied.<li/>";



    }

    public static final class UserRole {

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = " <li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded. <li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String userIds_description = "<li> This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String roles_description = "<li>This is a list of roles. Only records matching these roles will be included. <br/>If empty or not provided, no role-based filtering is applied.<li/>";

    }

    public static final class Principal {

    }

    public static final class Tenant {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the users that include the contents of the parameter in their names will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String codes_description = "<li> This is a list and determines which records we want to include in the response, based on their code. <br/>If not present, every record is included.<li/>";

    }

    public static final class TenantUser {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the users that include the contents of the parameter in their names will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String tenantIds_description = "<li> This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String userIds_description = "<li> This is a list and contains the ids of the users of the locks we want to include in the response. <br/>If empty, every record is included.<li/>";


    }

    public static final class PlanStatus {

        public static final String like_description = "<li> If there is a like parameter present in the query, only the entities that include the contents of the parameter in their labels will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String internalStatuses_description = "<li> This is a list and determines which records we want to include in the response, based on their internal status. The status can be <i>Draft</i> or <i>Finalized</i> or <i>Null</i>. We add 0 or 1 to the list respectively or null.<br/>If not present, every record is included.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";
    }

    public static final class DescriptionStatus{

        public static final String like_description = "<li> If there is a like parameter present in the query, only the entities that include the contents of the parameter in their labels will be in the response.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";

        public static final String internalStatuses_description = "<li> This is a list and determines which records we want to include in the response, based on their internal status. The status can be <i>Draft</i> or <i>Finalized</i> or <i>Null</i>. We add 0 or 1 to the list respectively or null.<br/>If not present, every record is included.<li/>";

        public static final String isActive_description = "<li> This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

    }

    public static final class PlanWorkflow {

        public static final String like_description = "<li>  If there is a like parameter present in the query, only the plan workflow entities that include the contents of the parameter in their labels will be in the response.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";
    }

    public static final class DescriptionWorkflow {

        public static final String like_description = "<li>  If there is a like parameter present in the query, only the description workflow entities that include the contents of the parameter in their labels will be in the response.<li/>";

        public static final String isActive_description = "<li>This is a list and determines which records we want to include in the response, based on if they are deleted or not. This filter works like this. If we want to view only the active records we pass [1] and for only the deleted records we pass [0].<br/>If not present or if we pass [0,1], every record is included.<li/>";

        public static final String ids_description = "<li> This is a list and contains the ids we want to include in the response. <br/>If empty, every record is included.<li/>";

        public static final String excludeIds_description = "<li> This is a list and contains the ids we want to exclude from the response. <br/>If empty, no record gets excluded.<li/>";
    }


}
