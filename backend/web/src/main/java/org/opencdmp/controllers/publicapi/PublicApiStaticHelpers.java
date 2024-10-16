package org.opencdmp.controllers.publicapi;

public final class PublicApiStaticHelpers {

    public static final class Plan {

        public static final String getPagedNotes = "The json response is of type **ResponseItem<DataTableData< DataManagementPlanPublicListingModel >>** containing the following properties:\n" +
                "<ol>" +
                "  <li><b>message</b>: string, message indicating error, null if everything went well</li>" +
                "  <li><b>statusCode</b>: integer, status code indicating if something unexpected happened, otherwise 0</li>" +
                "  <li><b>responseType</b>: integer, 0 for json, 1 for file</li>" +
                "  <li><b>payload</b>: DataTableData, containing the number of values of actual data returned and the data of type <b>DataManagementPlanPublicListingModel</b></li>" +
                "    <ol>" +
                "       <li>id: string, id of dmp returned</li>" +
                "       <li>label: string, label of dmp</li>" +
                "       <li>grant: string, grant of dmp</li>" +
                "       <li>createdAt: date, creation time of dmp</li>" +
                "       <li>modifiedAt: date, modification time of dmp</li>" +
                "       <li>version: integer, version of dmp</li>" +
                "       <li>groupId: uuid, group id in which dmp belongs</li>" +
                "       <li>users: list of UserInfoPublicModel, user who collaborated on the dmp</li>" +
                "       <li>researchers: list of ResearcherPublicModel, researchers involved in the dmp</li>" +
                "       <li>finalizedAt: date, finalization date</li>" +
                "       <li>publishedAt: date, publication date</li>" +
                "    </ol>" +
                "</ol>";

        public static final String getPagedResponseExample = "{\n" +
                "  \"statusCode\": 0,\n" +
                "  \"responseType\": 0,\n" +
                "  \"message\": null,\n" +
                "  \"payload\": {\n" +
                "    \"totalCount\": 2,\n" +
                "    \"data\": [\n" +
                "      {\n" +
                "        \"id\": \"e9a73d77-adfa-4546-974f-4a4a623b53a8\",\n" +
                "        \"label\": \"Giorgos's DMP\",\n" +
                "        \"grant\": \"Novel EOSC services for Emerging Atmosphere, Underwater and Space Challenges\",\n" +
                "        \"createdAt\": 1579077317000,\n" +
                "        \"modifiedAt\": 1586444334000,\n" +
                "        \"version\": 0,\n" +
                "        \"groupId\": \"d949592d-f862-4b31-a43a-f5f70596df5e\",\n" +
                "        \"users\": [],\n" +
                "        \"finalizedAt\": 1586444334000,\n" +
                "        \"publishedAt\": 1586444334000,\n" +
                "        \"hint\": \"dataManagementPlanListingModel\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"e27789f1-de35-4b4a-9587-a46d131c366e\",\n" +
                "        \"label\": \"TestH2020Clone3\",\n" +
                "        \"grant\": \"Evaluation of the Benefits of innovative Concepts of laminar nacelle and HTP installed on a business jet configuration\",\n" +
                "        \"createdAt\": 1600774437000,\n" +
                "        \"modifiedAt\": 1600879107000,\n" +
                "        \"version\": 0,\n" +
                "        \"groupId\": \"7b793c17-cb69-41d2-a97d-e8d1b03ddbed\",\n" +
                "        \"users\": [],\n" +
                "        \"finalizedAt\": 1600879107000,\n" +
                "        \"publishedAt\": 1600879107000,\n" +
                "        \"hint\": \"dataManagementPlanListingModel\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        public static final String getPagedRequestBodyDescription = "The dmpTableRequest is a DataManagementPlanPublicTableRequest object with the following fields:\n" +
                "<ul>" +
                " <li><b>length</b>: how many dmps should be fetched <i>(required)</i></li>" +
                " <li><b>offset</b>: offset of the returned dmps, first time should be 0, then offset += length</li>" +
                " <li><b>orderings</b>: array of strings specifying the order, format:= +string or -string or asc or desc.</li>" +
                "<b>+</b> means ascending order. <b>-</b> means descending order.<br>&nbsp;&nbsp;&nbsp;&nbsp;Available strings are: 1) status, 2) label, 3) publishedAt, 4) created.<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;<b>asc</b> equivalent to +label.<br>&nbsp;&nbsp;&nbsp;&nbsp;<b>desc</b> equivalent to -label.<br>" +
                "<li><b>criteria</b>: this is DataManagementPlanPublicCriteria object which applies filters for the dmp returned. More specifically:</li>" +
                "    <ol>" +
                "      <li>periodStart: date, dmps created date greater than periodStart</li>" +
                "      <li>periodEnd: date, dmps created date less than periodEnd</li>" +
                "      <li>grants: list of uuids, dmps with the corresponding grants</li>" +
                "      <li>grantsLike: list of strings, dmps fetched having their grant matching any of the strings provided</li>" +
                "      <li>funders: list of uuids, dmps with the corresponding funders</li>" +
                "      <li>fundersLike: list of strings, dmps fetched having their funders matching any of the strings provided</li>" +
                "      <li>datasetTemplates: list of uuids, dataset templates which are described in the dmps</li>" +
                "      <li>dmpOrganisations: list of strings, dmps belonging to these organisations</li>" +
                "      <li>collaborators: list of uuids, user who collaborated on the creation/modification of dmps</li>" +
                "      <li>collaboratorsLike: list of strings, dmps fetched having their collaborators matching any of the strings provided</li>" +
                "      <li>allVersions: boolean, if dmps should be fetched with all their versions</li>" +
                "      <li>groupIds: list of uuids, in which groups the dmps are</li>" +
                "      <li>like: string, dmps fetched have this string matched in their label or description</li>" +
                "    </ol>" +
                "<ul>";

        public static final String getPagedRequestParamDescription = "The fieldsGroup is a string which indicates if the returned objects would have all their properties\n" +
                "There are two available values: 1) listing and 2) autocomplete\n" +
                "<ul>" +
                " <li><b>listing</b>: returns objects with all their properties completed</li>" +
                " <li><b>autocomplete</b>: returns objects with only id, label, groupId and creationTime assigned</li>" +
                "<ul>";

        public static final String getOverviewSinglePublicNotes = "The json response is of type **ResponseItem< DataManagementPlanPublicModel >** containing the following properties:\n" +
                "<ol>" +
                " <li><b>message</b>: string, message indicating error, null if everything went well</li>" +
                " <li><b>statusCode</b>: integer, status code indicating if something unexpected happened, otherwise 0</li>" +
                " <li><b>responseType</b>: integer, 0 for json, 1 for file</li>" +
                " <li><b>payload</b>: DataManagementPlanPublicModel, dmp returned</li>" +
                "    <ol>" +
                "      <li>id: string, id of dmp returned</li>" +
                "      <li>label: string, label of dmp</li>" +
                "      <li>profile: string, profile of dmp</li>" +
                "      <li>grant: GrantPublicOverviewModel, grant of dmp</li>" +
                "      <li>createdAt: date, creation time of dmp</li>" +
                "      <li>modifiedAt: date, modification time of dmp</li>" +
                "      <li>finalizedAt: date, finalization date of dmp</li>" +
                "      <li>organisations: list of OrganizationPublicModel, organizations in which dmp belongs</li>" +
                "      <li>version: integer, version of dmp</li>" +
                "      <li>groupId: uuid, group id in which dmp belongs</li>" +
                "      <li>datasets: list of DatasetPublicModel, contained datasets</li>" +
                "      <li>associatedProfiles: list of AssociatedProfilePublicModel, associated profiles of dmp</li>" +
                "      <li>researchers: list of ResearcherPublicModel, researchers involved in dmp</li>" +
                "      <li>users: list of UserInfoPublicModel, user who collaborated on the dmp</li>" +
                "      <li>description: string, description of dmp</li>" +
                "      <li>publishedAt: date, publication date</li>" +
                "      <li>doi: string, if dmp has been published to zenodo so it has doi</li>" +
                "    </ol>" +
                "</ol>";

        public static final String getOverviewSinglePublicResponseExample = "{\n" +
                "  \"statusCode\": 0,\n" +
                "  \"responseType\": 0,\n" +
                "  \"message\": null,\n" +
                "  \"payload\": {\n" +
                "    \"id\": \"e9a73d77-adfa-4546-974f-4a4a623b53a8\",\n" +
                "    \"label\": \"Giorgos's DMP\",\n" +
                "    \"profile\": null,\n" +
                "    \"grant\": {\n" +
                "      \"id\": \"c8309ae5-4e56-43eb-aa5a-9950c24051fe\",\n" +
                "      \"label\": \"Novel EOSC services for Emerging Atmosphere, Underwater and Space Challenges\",\n" +
                "      \"abbreviation\": null,\n" +
                "      \"description\": null,\n" +
                "      \"startDate\": null,\n" +
                "      \"endDate\": null,\n" +
                "      \"uri\": null,\n" +
                "      \"funder\": {\n" +
                "        \"id\": \"25e76828-3539-4c66-9870-0ecea7a4d16e\",\n" +
                "        \"label\": \"European Commission||EC\",\n" +
                "        \"hint\": null\n" +
                "      },\n" +
                "      \"hint\": null\n" +
                "    },\n" +
                "    \"createdAt\": 1579077317000,\n" +
                "    \"modifiedAt\": 1586444334000,\n" +
                "    \"finalizedAt\": 1586444334000,\n" +
                "    \"organisations\": [],\n" +
                "    \"version\": 0,\n" +
                "    \"groupId\": \"d949592d-f862-4b31-a43a-f5f70596df5e\",\n" +
                "    \"datasets\": [\n" +
                "      {\n" +
                "        \"id\": \"853a24c3-def4-4978-985f-92e7fa57ef22\",\n" +
                "        \"label\": \"Giorgos's Dataset Desc\",\n" +
                "        \"reference\": null,\n" +
                "        \"uri\": null,\n" +
                "        \"description\": null,\n" +
                "        \"status\": 1,\n" +
                "        \"createdAt\": 1579077532000,\n" +
                "        \"dmp\": {\n" +
                "          \"id\": \"e9a73d77-adfa-4546-974f-4a4a623b53a8\",\n" +
                "          \"label\": \"Giorgos's DMP\",\n" +
                "          \"grant\": \"Novel EOSC services for Emerging Atmosphere, Underwater and Space Challenges\",\n" +
                "          \"createdAt\": 1579077317000,\n" +
                "          \"modifiedAt\": 1586444334000,\n" +
                "          \"version\": 0,\n" +
                "          \"groupId\": \"d949592d-f862-4b31-a43a-f5f70596df5e\",\n" +
                "          \"users\": [\n" +
                "            {\n" +
                "              \"id\": \"00476b4d-0491-44ca-b2fd-92e695062a48\",\n" +
                "              \"name\": \"OpenCDMP OpenCDMP\",\n" +
                "              \"role\": 0,\n" +
                "              \"email\": \"opencdmp@cite.gr\",\n" +
                "              \"hint\": \"UserInfoListingModel\"\n" +
                "            }\n" +
                "          ],\n" +
                "          \"finalizedAt\": 1586444334000,\n" +
                "          \"publishedAt\": 1586444334000,\n" +
                "          \"hint\": \"dataManagementPlanListingModel\"\n" +
                "        },\n" +
                "        \"datasetProfileDefinition\": {\n" +
                "          \"pages\": [...],\n" +
                "          \"rules\": [...],\n" +
                "          \"status\": 0\n" +
                "        },\n" +
                "        \"registries\": [],\n" +
                "        \"services\": [],\n" +
                "        \"dataRepositories\": [],\n" +
                "        \"tags\": null,\n" +
                "        \"externalDatasets\": [],\n" +
                "        \"profile\": {\n" +
                "          \"id\": \"2a6e0835-349e-412c-9fcc-8e1298ce8a5a\",\n" +
                "          \"label\": \"Horizon 2020\",\n" +
                "          \"hint\": null\n" +
                "        },\n" +
                "        \"modifiedAt\": 1579077898000,\n" +
                "        \"hint\": \"datasetOverviewModel\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"associatedProfiles\": [\n" +
                "      {\n" +
                "        \"id\": \"f41bd794-761d-4fe8-ab67-3a989d982c53\",\n" +
                "        \"label\": \"Swedish Research Council\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"2a6e0835-349e-412c-9fcc-8e1298ce8a5a\",\n" +
                "        \"label\": \"Horizon 2020\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"researchers\": [],\n" +
                "    \"users\": [\n" +
                "      {\n" +
                "        \"id\": \"00476b4d-0491-44ca-b2fd-92e695062a48\",\n" +
                "        \"name\": \"OpenCDMP OpenCDMP\",\n" +
                "        \"role\": 0,\n" +
                "        \"email\": \"opencdmp@cite.gr\",\n" +
                "        \"hint\": \"UserInfoListingModel\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"description\": null,\n" +
                "    \"publishedAt\": 1586444334000,\n" +
                "    \"doi\": \"10.5072/zenodo.522151\",\n" +
                "    \"hint\": \"dataManagementPlanOverviewModel\"\n" +
                "  }\n" +
                "}";

    }

    public static final class Description {

        public static final String getPagedNotes = "The json response is of type **ResponseItem<DataTableData< DatasetPublicListingModel >>** containing the following properties:\n" +
                "<ol>" +
                " <li><b>message</b>: string, message indicating error, null if everything went well</li>" +
                " <li><b>statusCode</b>: integer, status code indicating if something unexpected happened, otherwise 0</li>" +
                " <li><b>responseType</b>: integer, 0 for json, 1 for file</li>" +
                " <li><b>payload</b>: DataTableData, containing the number of values of actual data returned and the data of type <b>DatasetPublicListingModel</b></li>" +
                "   <ol>" +
                "     <li>id: string, id of dataset returned</li>" +
                "     <li>label: string, label of dataset</li>" +
                "     <li>grant: string, grant of dataset</li>" +
                "     <li>dmp: string, dmp description</li>" +
                "     <li>dmpId: string, dmp's id</li>" +
                "     <li>profile: DatasetProfilePublicModel, dataset's profile</li>" +
                "     <li>createdAt: date, creation date</li>" +
                "     <li>modifiedAt: date, modification date</li>" +
                "     <li>description: string, dataset's description</li>" +
                "     <li>finalizedAt: date, finalization date</li>" +
                "     <li>dmpPublishedAt: date, dmp's publication date</li>" +
                "     <li>version: integer, dataset's version</li>" +
                "     <li>users: list of UserInfoPublicModel, user who collaborated on the dataset</li>" +
                "   </ol>" +
                "</ol>";
        public static final String getPagedResponseExample = "{\n" +
                "  \"statusCode\": 0,\n" +
                "  \"responseType\": 0,\n" +
                "  \"message\": null,\n" +
                "  \"payload\": {\n" +
                "    \"totalCount\": 2,\n" +
                "    \"data\": [\n" +
                "      {\n" +
                "        \"id\": \"ef7dfbdc-c5c1-46a7-a37b-c8d8692f1c0e\",\n" +
                "        \"label\": \"BARKAMOL RIVOJLANGAN SHAXSNI TARBIYALASHDA  HARAKATLI O`YINLARNING O`RNI\",\n" +
                "        \"grant\": \"A next generation nano media tailored to capture and recycle hazardous micropollutants in contaminated industrial wastewater.\",\n" +
                "        \"dmp\": \"test for demo\",\n" +
                "        \"dmpId\": \"9dee6e72-7a4c-4fbd-b8a4-1f8cda38eb5e\",\n" +
                "        \"profile\": {\n" +
                "          \"id\": \"771283d7-a5be-4a93-bd3c-8b1883fe837c\",\n" +
                "          \"label\": \"Horizon Europe\",\n" +
                "          \"hint\": null\n" +
                "        },\n" +
                "        \"createdAt\": 1662711279000,\n" +
                "        \"modifiedAt\": 1662712928000,\n" +
                "        \"description\": \"<p>&nbsp; &nbsp; &nbsp;Annotatsiya &nbsp;Maqolada &nbsp;bolalarni &nbsp;o`yin &nbsp;mavjud &nbsp;bo`lgan &nbsp;shakllarda &nbsp;mavjud&nbsp;<br>\\nhayot &nbsp;bilan &nbsp;kengroq &nbsp;tanishtirishga &nbsp;imkon &nbsp;beradi. &nbsp;O`yin &nbsp;bolalarning &nbsp;turli &nbsp;xil&nbsp;<br>\\nfaoliyati,o`yin &nbsp;ko`nikmalarini &nbsp;shakllantirishga &nbsp;yordam &nbsp;beradi.Ularni &nbsp;fikrlash, &nbsp;his-<br>\\ntuyg`ular, tajribalar, o`yin muammosini hal qilishning faol usullarini izlash, ularning&nbsp;<br>\\no`yin sharoitlari va sharoitlariga bo`ysunishi, o`yindagi bolalarning munosabatlari,&nbsp;<br>\\no`yin orqali bola organik rivojlanadi, &nbsp;inson madaniyatining muhim qatlami kattalar&nbsp;<br>\\no`rtasidagi &nbsp;munosabatlar &nbsp;- &nbsp;oilada, &nbsp;ularning &nbsp;kasbiy &nbsp;faoliyati &nbsp;va &nbsp;boshqalar. &nbsp;O`yin&nbsp;<br>\\no`qituvchilar barcha ta&rsquo;lim vazifalarini, shu jumladan o`rganishni hal qiladigan eng&nbsp;<br>\\nmuhim faoliyat sifatida foydalaniladi.&nbsp;</p>\",\n" +
                "        \"finalizedAt\": 1662712928000,\n" +
                "        \"dmpPublishedAt\": 1662713226000,\n" +
                "        \"version\": 0,\n" +
                "        \"users\": [\n" +
                "          {\n" +
                "            \"id\": \"33024e48-d528-45a5-8035-ea48641bd2f2\",\n" +
                "            \"name\": \"DMP author\",\n" +
                "            \"role\": 0,\n" +
                "            \"email\": \"kanavou.p@gmail.com\",\n" +
                "            \"hint\": \"UserInfoListingModel\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"hint\": \"datasetListingModel\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"0f253ab2-18cb-4798-adc1-135b81cfad0c\",\n" +
                "        \"label\": \"A \\\"zoom-elit\\\" és a kamionosok küzdelme, avagy a meritokrácia és a populizmus összecsapása\",\n" +
                "        \"grant\": \"Discovery Projects - Grant ID: DP140100157\",\n" +
                "        \"dmp\": \"TEST UPDATE 2.8.2022\",\n" +
                "        \"dmpId\": \"1f4daa8f-4e2f-4dc9-a60b-f6b75d313400\",\n" +
                "        \"profile\": {\n" +
                "          \"id\": \"3d43ba45-25fa-4815-81b4-9bf22ecd8316\",\n" +
                "          \"label\": \"HE_Final\",\n" +
                "          \"hint\": null\n" +
                "        },\n" +
                "        \"createdAt\": 1659392761000,\n" +
                "        \"modifiedAt\": 1659393655000,\n" +
                "        \"description\": \"<p>A kanadai kamionosok &bdquo;szabads&aacute;gmenete&rdquo; kapcs&aacute;n a&nbsp;New York Times&nbsp;has&aacute;bjain Ross Donthat publicista egy r&eacute;gi k&ouml;nyvre h&iacute;vja fel a figyelmet, amely sok &eacute;vtizeddel ezelőtt megj&oacute;solta az elit elleni hasonl&oacute; l&aacute;zad&aacute;sokat.</p>\",\n" +
                "        \"finalizedAt\": 1659393654000,\n" +
                "        \"dmpPublishedAt\": 1659393698000,\n" +
                "        \"version\": 0,\n" +
                "        \"users\": [\n" +
                "          {\n" +
                "            \"id\": \"33024e48-d528-45a5-8035-ea48641bd2f2\",\n" +
                "            \"name\": \"DMP author\",\n" +
                "            \"role\": 0,\n" +
                "            \"email\": \"kanavou.p@gmail.com\",\n" +
                "            \"hint\": \"UserInfoListingModel\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"hint\": \"datasetListingModel\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        public static final String getPagedRequestBodyDescription = "The datasetTableRequest is a DatasetPublicTableRequest object with the following fields:\n" +
                "<ul>" +
                "<li><b>length</b>: how many datasets should be fetched <i>(required)</i></li>" +
                "<li><b>offset</b>: offset of the returned datasets, first time should be 0, then offset += length</li>" +
                "<li><b>orderings</b>: array of strings specifying the order, format:= +string or -string or asc or desc.</li>" +
                "<b>+</b> means ascending order. <b>-</b> means descending order.<br>&nbsp;&nbsp;&nbsp;&nbsp;Available strings are: 1) status, 2) label, 3) created.<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;<b>asc</b> equivalent to +label.<br>&nbsp;&nbsp;&nbsp;&nbsp;<b>desc</b> equivalent to -label.<br>" +
                "<li><b>criteria</b>: this is DatasetPublicCriteria object which applies filters for the datasets returned. More specifically:</li>" +
                "  <ol>" +
                "    <li>periodStart: date, datasets created date greater than periodStart</li>" +
                "    <li>periodEnd: date, datasets created date less than periodEnd</li>" +
                "    <li>grants: list of uuids, dmps(datasets) with the corresponding grants</li>" +
                "    <li>collaborators: list of uuids, user who collaborated on the creation/modification of datasets</li>" +
                "    <li>datasetTemplates: list of uuids, dataset templates uuids to be included</li>" +
                "    <li>dmpOrganisations: list of strings, datasets involved in dmps which belong to these organisations</li>" +
                "    <li>tags: list of Tag objects, tags involved in datasets</li>" +
                "    <li>dmpIds: list of uuids, dmps with the specific ids</li>" +
                "    <li>groupIds: list of uuids, in which groups the datasets are</li>" +
                "    <li>allVersions: boolean, if datasets should be fetched with all their versions</li>" +
                "    <li>like: string, datasets fetched have this string matched in their label or description</li>" +
                "  </ol>" +
                "</ul>";

        public static final String getOverviewSinglePublicNotes = "The json response is of type **ResponseItem< DatasetPublicModel >** containing the following properties:\n" +
                "<ol>" +
                " <li><b>message</b>: string, message indicating error, null if everything went well</li>" +
                " <li><b>statusCode</b>: integer, status code indicating if something unexpected happened, otherwise 0</li>" +
                " <li><b>responseType</b>: integer, 0 for json, 1 for file</li>" +
                " <li><b>payload</b>: DatasetPublicModel, dmp returned</li>" +
                "    <ol>" +
                "      <li>id: uuid, id of dataset returned</li>" +
                "      <li>label: string, label of dataset</li>" +
                "      <li>reference: string, reference of dataset</li>" +
                "      <li>uri: string, uri of dataset</li>" +
                "      <li>description: string, dataset's description</li>" +
                "      <li>status: string, dataset's status</li>" +
                "      <li>createdAt: date, creation time of dataset</li>" +
                "      <li>dmp: DataManagementPlanPublicListingModel, dmp to which dataset belongs</li>" +
                "      <li>datasetProfileDefinition: PagedDatasetProfile, dataset's paged description</li>" +
                "      <li>registries: list of RegistryPublicModel, dataset's registries</li>" +
                "      <li>services: list of ServicePublicModel, dataset's services</li>" +
                "      <li>dataRepositories: list of DataRepositoryPublicModel, dataset's data repositories</li>" +
                "      <li>tags: list of Tag, dataset's tags</li>" +
                "      <li>externalDatasets: list of ExternalDatasetPublicListingModel, dataset's external datasets</li>" +
                "      <li>profile: DatasetProfilePublicModel, dataset's profile</li>" +
                "      <li>modifiedAt: date, modification time of dataset</li>" +
                "   </ol>" +
                "</ol>";
        public static final String getOverviewSinglePublicResponseExample = "{\n" +
                "  \"statusCode\": 0,\n" +
                "  \"responseType\": 0,\n" +
                "  \"message\": null,\n" +
                "  \"payload\": {\n" +
                "    \"id\": \"ef7dfbdc-c5c1-46a7-a37b-c8d8692f1c0e\",\n" +
                "    \"label\": \"BARKAMOL RIVOJLANGAN SHAXSNI TARBIYALASHDA  HARAKATLI O`YINLARNING O`RNI\",\n" +
                "    \"reference\": null,\n" +
                "    \"uri\": null,\n" +
                "    \"description\": \"<p>&nbsp; &nbsp; &nbsp;Annotatsiya &nbsp;Maqolada &nbsp;bolalarni &nbsp;o`yin &nbsp;mavjud &nbsp;bo`lgan &nbsp;shakllarda &nbsp;mavjud&nbsp;<br>\\nhayot &nbsp;bilan &nbsp;kengroq &nbsp;tanishtirishga &nbsp;imkon &nbsp;beradi. &nbsp;O`yin &nbsp;bolalarning &nbsp;turli &nbsp;xil&nbsp;<br>\\nfaoliyati,o`yin &nbsp;ko`nikmalarini &nbsp;shakllantirishga &nbsp;yordam &nbsp;beradi.Ularni &nbsp;fikrlash, &nbsp;his-<br>\\ntuyg`ular, tajribalar, o`yin muammosini hal qilishning faol usullarini izlash, ularning&nbsp;<br>\\no`yin sharoitlari va sharoitlariga bo`ysunishi, o`yindagi bolalarning munosabatlari,&nbsp;<br>\\no`yin orqali bola organik rivojlanadi, &nbsp;inson madaniyatining muhim qatlami kattalar&nbsp;<br>\\no`rtasidagi &nbsp;munosabatlar &nbsp;- &nbsp;oilada, &nbsp;ularning &nbsp;kasbiy &nbsp;faoliyati &nbsp;va &nbsp;boshqalar. &nbsp;O`yin&nbsp;<br>\\no`qituvchilar barcha ta&rsquo;lim vazifalarini, shu jumladan o`rganishni hal qiladigan eng&nbsp;<br>\\nmuhim faoliyat sifatida foydalaniladi.&nbsp;</p>\",\n" +
                "    \"status\": 1,\n" +
                "    \"createdAt\": 1662711279000,\n" +
                "    \"dmp\": {\n" +
                "      \"id\": \"9dee6e72-7a4c-4fbd-b8a4-1f8cda38eb5e\",\n" +
                "      \"label\": \"test for demo\",\n" +
                "      \"grant\": \"A next generation nano media tailored to capture and recycle hazardous micropollutants in contaminated industrial wastewater.\",\n" +
                "      \"createdAt\": 1662710691000,\n" +
                "      \"modifiedAt\": 1662713226000,\n" +
                "      \"version\": 0,\n" +
                "      \"groupId\": \"adaa4e17-7375-45b8-b052-09edaeb6da86\",\n" +
                "      \"users\": [\n" +
                "        {\n" +
                "          \"id\": \"33024e48-d528-45a5-8035-ea48641bd2f2\",\n" +
                "          \"name\": \"DMP author\",\n" +
                "          \"role\": 0,\n" +
                "          \"email\": \"kanavou.p@gmail.com\",\n" +
                "          \"hint\": \"UserInfoListingModel\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"finalizedAt\": 1662713226000,\n" +
                "      \"publishedAt\": 1662713226000,\n" +
                "      \"hint\": \"dataManagementPlanListingModel\"\n" +
                "    },\n" +
                "    \"datasetProfileDefinition\": {\n" +
                "      \"pages\": [...],\n" +
                "      \"rules\": [...],\n" +
                "      \"status\": 0\n" +
                "    },\n" +
                "    \"registries\": [],\n" +
                "    \"services\": [],\n" +
                "    \"dataRepositories\": [],\n" +
                "    \"tags\": null,\n" +
                "    \"externalDatasets\": [],\n" +
                "    \"profile\": {\n" +
                "      \"id\": \"771283d7-a5be-4a93-bd3c-8b1883fe837c\",\n" +
                "      \"label\": \"Horizon Europe\",\n" +
                "      \"hint\": null\n" +
                "    },\n" +
                "    \"modifiedAt\": 1662712928000,\n" +
                "    \"hint\": \"datasetOverviewModel\"\n" +
                "  }\n" +
                "}";

    }

}
