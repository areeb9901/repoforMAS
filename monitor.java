This is my task:
On the Beagle Monitor Page - System Status Checks, please could we add a line for

Checking Docassemble API key validity: OK (Green) or No longer valid - please create a new key (Alert icon in Red)

Steps to check if an api key is valid

1. Get using the API key from the table LDM_TB_DOCASSEMBLE_APIKEY

2. result = https://docassemble.group.echonet/api/list?key=API key

3. if the result = "[]" then the key is valid, otherwise it is invalid.


How to do this?

This is the .vm page of it:
#macro(printResult $text $result)

<tr>

<th class="w-25">$text</th>

<td class="alert" style="background-color: ${result.background}">

#if(${result.status)=="OK")

<i class='material-icons md-21 text-color-green'>check_circle</i>

#else

<i class="material-icons md-21 icon-red">warning</i>

#end

&nbsp; ${result.status) </td>

</tr>

#end

<div class="card">

<div class="card-header card-header-admin card-header-icon">

<div class="card-icon">

<i class="material-icons icon-white">desktop_mac</i>

</div>

<h4 class="card-title"><b>Beagle Monitor Page</b></h4>

I

</div>

<div class="card-body">

<div class="container-fluid">

<div class="row">

<div class="col-md-12">

<div class="card card-low-shadow w-90">

<div class="card-header card-header-bnp">

<div class="card-title">System Status Checks</div>

</div>

<div class="card-body">

<div class="card card-low-shadow">

<table class="table table-borderless table-responsive-sm w-100">

#printResult("Checking connection to Beagle database" $connectionToCoreOK)

$connectionToDocumentsOK) #printResult("Checking connection to Negotiation Document Server"

#printResult( "Checking connection to LDAP Server" $connectionToUserServiceOK)

#printResult("Checking connection to Feed "$connectionToCrds FeedOK)

#printResult( "Checking Indexes" $indexInSyncOK)

<tr>

<td></td>

<td>

<table class="table table-striped table-bordered">

<tr>

<th>Index</th>

<th>Document Count</th>

<th>Database Count</th>

</tr>

#foreach($temp in $action.getIndexes())

<tr>

<td>$temp.getIndex().getName()</td>

<td>$temp.getDocumentCount()</td>

<td>$temp.getDbCount()</td>

</tr>

#end

</table>

</td>

</tr>

</table>

</div>

</div>

</div>

<br><br>

<div class="card card-low-shadow w-90">

<div class="card-header card-header-bnp">

<div class="card-title">Summary</div>

</div>

<div class="card-body">

<div class="card card-low-shadow">

<table class="table table-borderless table-responsive-sm w-100">

<tr>

<th class="w-25">Last Increment Index Date</th>

<td>$lastIncrementalIndexDate</td>

</tr>

#set ($overallResult = $everythingOK)

<tr>

<th>"Overall Result"</th>

<td style="background-color: ${overallResult.background}">

#if($(overallResult.status)=="OK")

<i class='material-icons md-21 text-color-green'>check_circle</i>&nbsp; Status: Normal

#else

<i class="material-icons md-21 icon-red">warning</i>&nbsp; Status: Critical

#end

</td>

</tr>

</table>

I</div>

</div>

</div>

</div>

</div>

</div>

</div>

</div>


This is the .java file :

package com.bnpparibas.beagle.kernel.actions;

import java.util.Map;

import java.util.Set;

import org.apache.struts2.interceptor.SessionAware;

import com.bnpparibas.beagle.indexing. IndexInfo;

import com.bnpparibas.beagle.indexing. IndexNotInSync;

import com.bnpparibas.beagle.indexing. Indexing Service;

import com.bnpparibas.beagle.kernel.feed.FeedService;

import com.bnpparibas.beagle.kernel.ldap.AuthenticationFailureException;

import com.bnpparibas.beagle.kernel.ldap. InvalidSearchException;

import com.bnpparibas.beagle.kernel.ldap. UserNotFoundException;

import com.bnpparibas.beagle.kernel.policy. AuthorisationPolicy;

import com.bnpparibas.beagle.kernel.policy. BeagleFeatures;

import com.bnpparibas.beagle.kernel.policy.Policy;

import com.bnpparibas.beagle.kernel.security. UserNotAuthorised Exception;

import com.bnpparibas.beagle.kernel.security. UserStillPendingException;

import com.bnpparibas.beagle.kernel.services.FileSystemAccess;

import com.bnpparibas.beagle.ma.model.MasterAgreement;

import com.bnpparibas.beagle.staticdata.model.User;

public class Monitor extends BeagleGuestActionSupport implements SessionAware {

private static final Result OK = new Result(true);

private static final Result FAILED = new Result(false);

private FileSystemAccess fileSystemAccess;

private IndexingService indexingService;

private FeedService feedService;

private Result connectionToCoreOK;

private Result connectionToDocumentsOK;

private Result connectionToUserServiceOK;

private Result connectionToCrdsFeed0K;

private Result connectionToDocassembleAPIkey;

private Result indexInSync;

private Set<IndexInfo> indexes;

private static final Long MASTER_AGREEMENT_ID = (long) 3061;

private Map session;

private Policy policy;

private boolean debug = false;

@Override

public String execute() {

setPolicy();

}

checkDatabase();

checkDocuments();

checkUserService();

checkCrdsFeed();

checkIndexInSync();

return SUCCESS;

}
private void checkUserService() {

try {

userService.authenticate("whatever", "nonblankpassword");

connectionToUserServiceOK = FAILED;

} catch (AuthenticationFailureException e) {

connectionToUserServiceOK = OK;

} catch (UserNotFoundException e) {

connectionToUserServiceOK = OK;

} catch (InvalidSearchException e) {

connectionToUserServiceOK = OK;

} catch (UserNotAuthorisedException e) {

connectionToUserServiceOK = OK;

} catch (UserStillPendingException e) {

connectionToUserServiceOK = OK;

} catch (Exception t) {

connectionToUserServiceOK = FAILED;

}

}

I

private void checkDocuments() {

try {

connectionToDocumentsOK = new Result(fileSystemAccess.checkFullAccess());

} catch (Exception t) {

connectionToDocumentsOK = FAILED;

}

}

private void checkDatabase() {

try {

MasterAgreement.get(repository, MASTER_AGREEMENT_ID);

connectionToCoreOK = OK;

} catch (Exception t) {

connectionToCoreOK =FAILED;
}
}
private void checkDatabase() {

try {

MasterAgreement.get(repository, MASTER_AGREEMENT_ID);

connectionToCoreOK = OK;

} catch (Exception t) {

}

}

connectionToCoreOK FAILED;

private void check IndexInSync() {

try {

indexingService.checkIndexesAreInSync();

} catch (IndexNotInSync indexStatus) {

this.indexInSync = new Result(indexStatus.getMessage());

indexes indexStatus.getIndexes();

if(indexStatus.is Indexes AreInSync()) {
this.indexInSync = OK;
}

}

}
private void checkCrdsFeed() {

} connectionToCrdsFeedOK = new Result (feedService != null && feedService.isActive());

public Result getIndexInSyncOK() { } return indexInSync;

public Result getConnectionToCoreOK() { } return connectionToCoreOK;

public Result getConnectionToDocumentsOK() { } return connection ToDocumentsOK;

public Result getConnectionToUserServiceOK() { return connectionToUserServiceOK;

}

public Result getConnectionToCrdsFeedOK() { return connectionToCrdsFeedOK;

}
public Result getEverythingOK() {

}

return new Result(connectionStatus ToCoreAndDocumentsIsOk() && connectionToUserServiceOK.isok() && connectionStatusToUserServiceAndCrds FeedIsOK());

public boolean connectionStatus ToCoreAndDocumentsIsOk(){

return connectionToCoreOK.isok() && connection ToDocumentsOK.isok();

}

public boolean connectionStatusToUserServiceAndCrdsFeedIsOK(){

}

return connectionToCrdsFeedOK.isok() && indexInSync.isok();

public void setFileSystemAccess (FileSystemAccess fileSystemAccess) {

this.fileSystemAccess = fileSystemAccess;

}

public Set<IndexInfo> getIndexes() {

}

return indexes;

public void setIndexingService (IndexingService indexingService) {

}

this.indexingService = indexingService;

public void setFeedService(FeedService feedService) {

}

this.feedService = feedService;

I

@Override

public void setSession(Map session){
this.session=session;
}
public User getUser() { } return User.getUser(this.session);

public Policy getPolicy() { } return this.policy;

private Policy getBasePolicy() { } return new BeagleFeatures();

private void setPolicy() { Policy p = getBasePolicy(); User user = getUser(); policy = new AuthorisationPolicy (user, p,repository);

}

public void setDebug (boolean debug) {

}

this.debug = debug; this.indexingService.setDebug(debug);

public static class Result {

private static final String RED = "#FFCCCC"; private static final String GREEN = "#CCFFCC";

private String comment;

private String background = RED;
public Result (boolean status) { this(status? "OK": "FAILED"); this.background = status? GREEN: RED; }

public Result(String comment) { this.comment = comment;

}

public String getStatus() { return comment; }

public boolean isok() { return "OK".equals(comment); }

public String getBackground() { return background;

}

}



}



If you need info of other page, please tell me i will provide you with the code of other required pages as well.


I also have one big file BeagleRepositoryImpl.java in which there is this function:
@Override

public String getDocAssembleApiKey() {

SQLQuery query = getSession().createSQLQuery("SELECT API_KEY FROM LDM_TB_DOCASSEMBLE_APIKEY");

Object apiKey = query.uniqueResult();

if (null! apiKey) {

return apikey.toString();

}

return "";

}


I also have one file DocAssembleAPIService.java and its code is:

package com.bnpparibas.beagle.docassemble.services;

import com.bnpparibas.beagle.kernel.restclient. RestResponseErrorHandler;

import com.bnpparibas.beagle.kernel.restclient.RestTemplateBuilder;

import org.codehaus.jackson.map.ObjectMapper;

import org.codehaus.jackson.map.DeserializationConfig;

import org.springframework.http.HttpEntity;

import org.springframework.http.HttpMethod;

import org.springframework.http. ResponseEntity;

import org.springframework.web.client. Response ErrorHandler;

import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponents;

import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;

import com.bnpparibas.beagle.parameters Assembler.constants. Basic Parameters;

import java.io.*;

public class DocAssembleAPIService {
private String docAssembleApiUrl;

private RestTemplateBuilder restTemplateBuilder;

private String docAssembleInterviewUrl;

private String filePath;

public <T> T getInterviewDetails (String apikey, String sessionID, Class<T> targetType, String templateUrl) throws IOException {

}

ObjectMapper objectMapper = new ObjectMapper();

RestTemplate restTemplate restTemplateBuilder

.init()

.setErrorHandler(getErrorHandler())

.getRestTemplate();

HttpEntity<String> entity = getDocAssembleAPIKey (apiKey);

String url = docAssembleApiUrl + BasicParameters.DOCASSEMBLE_INTERVIEW_DET_URL;

UriComponents builder UriComponentsBuilder.fromHttpUrl(url)

.queryParam(BasicParameters. TEMPLATE_URL_LABEL, templateUrl)

.queryParam(BasicParameters.SESSION_LABEL, sessionID)

.build();

ResponseEntity<String> response = restTemplate.exchange (builder.toString(),

HttpMethod.GET, entity, String.class);

objectMapper.configure (DeserializationConfig. Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

objectMapper.enable (DeserializationConfig. Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

return targetType.cast(objectMapper.readValue(response.getBody(), targetType));
}
private HttpEntity<String> getDocAssembleAPIKey (String docAssembleApiKey) {

HttpHeaders httpHeaders = new HttpHeaders();

httpHeaders.setContentType(MediaType.APPLICATION_JSON);

httpHeaders.set(BasicParameters.DOCASSEMBLE_API_KEY, docAssembleApiKey);

return new HttpEntity<>(httpHeaders);

}

public File retriveStoredFiles (String apikey, int docNum, String interviewTemp, String sessionID, String docName) throws IOException {

FileOutputStream os = null;

RestTemplate restTemplate restTemplateBuilder

.init()

.setErrorHandler(getErrorHandler())

.getRestTemplate();

HttpEntity<String> entity = getDocAssembleAPIKey(apiKey);

String url = docAssembleApiUrl + BasicParameters.DOCASSEMBLE_GET_DOC_URL + docNum;

UriComponents builder UriComponentsBuilder.fromHttpUrl(url)

.queryParam(BasicParameters.TEMPLATE_URL_LABEL, interviewTemp)

.queryParam(BasicParameters.SESSION_LABEL, sessionID)

.build();

ResponseEntity<byte[]> response = restTemplate.exchange(builder.toString(),

HttpMethod.GET, entity, byte[].class);

File file = new File(filePath + docName);

try {

os = new FileOutputStream(file);

os.write(response.getBody());

} finally {

if(null != os) {

os.close();

}

}

return file;

}

private Response ErrorHandler getErrorHandler() {

return new RestResponseErrorHandler();

}

public String getDocAssembleApiUrl() {

}

return docAssembleApiUrl;

public void setDocAssembleApiUrl (String docAssembleApiUrl) { this.docAssembleApiUrl = docAssembleApiUrl;

}

public RestTemplateBuilder getRestTemplateBuilder() {

return restTemplateBuilder;
}

public void setRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder) {

this.restTemplateBuilder = restTemplateBuilder;

}

public String getDocAssembleInterviewUrl() {

return docAssembleInterviewUrl;

}

I

public void setDocAssembleInterviewUrl(String docAssemble InterviewUrl) { this.docAssembleInterviewUrl = docAssembleInterviewUrl;

}

public void setFilePath(String filePath) {

this.filePath = filePath;

}

}


Now please figure out what needs to be done and where and if you need more code files then let me know.