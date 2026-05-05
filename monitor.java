This is NegotiationDocument.java


package com.bnpparibas.beagle.ma.model;

import java.io.Serializable;

import java.util.Date;

import java.util.List;

import java.util.Arrays;

import com.bnpparibas.beagle.documents.DocumentClass;

import com.bnpparibas.beagle.documents.NonStandardNegoDocument;

import com.bnpparibas.beagle.documents.StandardNegoDocument;

import com.bnpparibas.beagle.documents.Transition;

import com.bnpparibas.beagle.documents.actions.AddCorrespondence;

import com.bnpparibas.beagle.documents.model.NegoDocEmailSaved;

import com.bnpparibas.beagle.kernel.model.Auditable;

import com.bnpparibas.beagle.kernel.model.EntityType;

import com.bnpparibas.beagle.kernel.util.BeagleStringUtils;

import com.bnpparibas.beagle.kernel.util.CompareUtil;

import com.bnpparibas.beagle.kernel.util.ListBuilder;

import com.bnpparibas.beagle.staticdata.model.Location;

import com.bnpparibas.beagle.staticdata.model.NegotiationDocumentCategories;

import com.bnpparibas.beagle.staticdata.model.NegotiationDocumentSummaries;

import com.bnpparibas.beagle.staticdata.model.NegotiationDocumentType;

import com.bnpparibas.beagle.staticdata.model.Negotiator;

import com.bnpparibas.beagle.staticdata.model.User;

public class NegotiationDocument extends Auditable implements Comparable, Serializable, EntityType {

public static final String FOR_AGREEMENT = "NegotiationDocumentForAgreement";

public static final String FOR_GROUP = "NegotiationDocumentForGroup";

public static final String INTERNAL_SCOPE = "internal";

public static final String EXTERNAL_SCOPE = "external";

public static final String SEND_DIRECTION = "send";

public static final String RECEIVE_DIRECTION = "receive";   

private static final List EXECUTED_DOCUMENTATION_TYPES = Arrays.asList(new Long("8"),new Long("9"),new Long("11"),new Long("30"));

//    public static final List<Long> BNPA_SA_ENTITIES = Arrays.asList(1070l,5149l,456695l,306583l);

protected Long containerId;

protected Long id;

protected Negotiator negotiator;

protected String fileName;

protected String summary;

protected String detail;

protected String documentSize;

protected Date documentDate;

protected NegotiationDocumentType type;

protected String tabId = NegotiationDocumentType.CORRESPONDENCE_TYPE_ID;

protected String forGroup;

protected String scope;

protected String direction;

protected boolean isDocPresentForCollOps;

protected String uploaderUid;

protected User uploaderDetails;

protected Boolean deletedFlag;

protected Boolean mailSent = Boolean.FALSE;

protected String currentValidationState;

protected NegotiationDocumentType documentCatagory;

protected NegotiationDocumentCategories documentCategories;

protected NegotiationDocumentSummaries documentSummary;

protected Boolean isStandard;

protected DocumentClass documentClass = DocumentClass.NONE;

protected String emailNotificationsComments;

protected CSAEmailSaved emailSent;

protected String documentComment;

protected Long documentGroupId;

protected NegoDocEmailSaved docEmailSent;

protected String segmentId ;

protected Location cmoLocation;

protected boolean isSharedDocument;

protected User drafterDetails;

protected String drafterUid;

protected String docStoreFileId;

protected Boolean isDocumentPresentInDocstore;

public Boolean getIsDocumentPresentInDocstore() {

return isDocumentPresentInDocstore;

}

public void setIsDocumentPresentInDocstore(Boolean isDocumentPresentInDocstore) {

this.isDocumentPresentInDocstore = isDocumentPresentInDocstore;

}

public NegotiationDocument() {

}

public NegotiationDocument(Long containerId,

NegotiationDocumentType type,

Negotiator negotiator,

User uploaderDetails,

Date documentDate,

String summary,

String detail,

String direction,

String scope,

String fileName,

int fileLengthInKb,

boolean forGroup, NegotiationDocumentType documentCatagory, Boolean isStandard,String comment,String documentComment,Long documentGroupId, String segmentId, Location cmoLocation) {

validate(type, scope, direction);

this.containerId = containerId;

this.forGroup = BeagleStringUtils.toYorN(forGroup);

this.type = type;

setUploader(uploaderDetails);

this.documentDate = documentDate;

this.summary = summary;

this.fileName = fileName;

this.detail = detail;

this.negotiator = negotiator;

setDocumentSize(fileLengthInKb);

updateTabId(type);

setScope(scope);

setDirection(direction);

this.documentCatagory = documentCatagory;

this.isStandard = isStandard;

this.emailNotificationsComments = comment;

this.documentComment = documentComment;

this.documentGroupId = documentGroupId;

this.segmentId = segmentId;

this.cmoLocation = cmoLocation;

}

public NegotiationDocument(String tabId) {

this.tabId = tabId;

}

public CSAEmailSaved getEmailSent() {

return emailSent;

}

public void setEmailSent(CSAEmailSaved emailSent) {

this.emailSent = emailSent;

}

public String getEmailNotificationsComments() {

return emailNotificationsComments;

}public void setEmailNotificationsComments(String emailNotificationsComments) {

this.emailNotificationsComments = emailNotificationsComments;

}

public Boolean getIsStandard() {

return isStandard;

}

public void setIsStandard(Boolean standard) {

this.isStandard = standard;

if (isStandard == null) {

this.documentClass = DocumentClass.NONE;

documentClass.setNegotiationDocument(this);

} else {

if (isStandard) {

this.documentClass = new StandardNegoDocument();

documentClass.setNegotiationDocument(this);

} else {

this.documentClass = new NonStandardNegoDocument();

documentClass.setNegotiationDocument(this);

}

}

}

public String getDocStoreFileId() {

return docStoreFileId;

}

public void setDocStoreFileId(String docStoreFileId) {

this.docStoreFileId = docStoreFileId;

}

public NegotiationDocumentType getDocumentCatagory() {

return documentCatagory;

}

public void setDocumentCatagory(NegotiationDocumentType documentCatagory) {

this.documentCatagory = documentCatagory;

}

public NegotiationDocumentCategories getDocumentCategories() {

return documentCategories;

}

public void setDocumentCategories(NegotiationDocumentCategories documentCategories) {

this.documentCategories = documentCategories;

}

public static List getExecutedDocumentationTypes(){

return EXECUTED_DOCUMENTATION_TYPES;

}

public Boolean isDeletedFlag() {

return deletedFlag;

}

public void setDeletedFlag(Boolean deletedFlag) {

this.deletedFlag = deletedFlag;

}

private static void validate(NegotiationDocumentType type, String scope, String direction) {

if (type == null || type.isExecutedDocument()) {

return;

}

if (scope != null && !EXTERNAL_SCOPE.equals(scope) && !INTERNAL_SCOPE.equals(scope)) {

throw new IllegalArgumentException("Invalid scope value: " + scope);

}if (direction != null && !SEND_DIRECTION.equals(direction) && !RECEIVE_DIRECTION.equals(direction)) {

throw new IllegalArgumentException("Invalid direction value: " + direction);

}

}

public Long getId() {

return id;

}

public void setId(Long id) {

this.id = id;

}

public Negotiator getNegotiator() {

return negotiator;

}

public void setNegotiator(Negotiator negotiator) {

this.negotiator = negotiator;

}

public void setFileName(String fileName) {

this.fileName = fileName;

}

public String getFileName() {

if (fileName != null) {

return fileName.substring(fileName.lastIndexOf("\\") + 1);

} else {

return null;

}

}

public String getSummary() {

return summary;

}

public void setSummary(String summary) {

this.summary = summary;

}

public String getDetail() {

return detail;

}

public void setDetail(String detail) {

this.detail = detail;

}

public Date getDocumentDate() {

return documentDate;

}

public void setDocumentDate(Date documentDate) {

this.documentDate = documentDate;

}

public NegotiationDocumentType getType() {

return type == null ? NegotiationDocumentType.getUnknownTypeForTab(tabId) : type;

}

public void setType(NegotiationDocumentType type) {

this.type = type;

updateTabId(type);

}

public List getGroupingKey() {

return ListBuilder.build(containerId, type != null ? type.getId() : "null",

negotiator.getId(), tabId, documentDate, summary, detail,documentCatagory,currentValidationState,documentClass.toString(),uploaderDetails);

}

@Override

public int compareTo(Object o) {

int c = CompareUtil.nullSafeCompareTo(documentDate, ((NegotiationDocument) o).documentDate);

if (c == 0) {

c = CompareUtil.nullSafeCompareTo(summary, ((NegotiationDocument) o).summary);

}

if (c == 0) {

c = CompareUtil.nullSafeCompareTo(id, ((NegotiationDocument) o).id);

}

return c == 0 ? 1 : -c;

}

@Override

public String toString() {

return getId() + " " + getGroupingKey();

}

public String getUploaderUid() {

return uploaderUid;

}

public String getDrafterUid() {

return drafterUid;

}

/**

* Be careful, it can be null (if the uploader UID does not match any Beagle User)

*/

public User getUploader() {

return uploaderDetails;

}

public void setUploader(User uploader) {

this.uploaderUid = uploader.getId();

this.uploaderDetails = uploader;

}

public User getDrafter() {

return drafterDetails;

}

public void setDrafter(User drafter) {

this.drafterUid = drafter.getId();

this.drafterDetails = drafter;

}

public boolean isForGroup() {

return BeagleStringUtils.isY(forGroup);

}

public void setForGroup(String forGroup) {

this.forGroup = forGroup;

}public void isForGroup(boolean forGroup) {

this.forGroup = BeagleStringUtils.toYorN(forGroup);

}

public Long getContainerId() {

return containerId;

}

public void setContainerId(Long containerId) {

this.containerId = containerId;

}

public boolean isSharedDocument() {

return isSharedDocument;

}

public void setIsSharedDocument(boolean isSharedDocument) {

this.isSharedDocument = isSharedDocument;

}

public void setScope(String scope) {

if (scope == null || getType().isExecutedDocument()) {

this.scope = null;

} else {

this.scope = EXTERNAL_SCOPE.equals(scope) ? "Y" : "N";

}

}

public void setDirection(String direction) {

if (direction == null || getType().isExecutedDocument()) {

this.direction = null;

} else {

this.direction = SEND_DIRECTION.equals(direction) ? "Y" : "N";

}

}

public String getScope() {

if (scope == null) {

return null;

}

switch (scope.charAt(0)) {

case 'Y':

return EXTERNAL_SCOPE;

case 'N':

return INTERNAL_SCOPE;

default:

throw new IllegalStateException("Scope may be Y, N or null: was " + scope);

}

}

public String getDirection() {

if (direction == null) {

return null;

}

switch (direction.charAt(0)) {

case 'Y':

return SEND_DIRECTION;

case 'N':

return RECEIVE_DIRECTION;

default:

throw new IllegalStateException("Direction may be Y, N or null: was " + direction);

}

}

public void removeFile() {

this.fileName = null;

this.documentSize = null;

}

public void setDocumentSize(int fileLengthInKb) {

this.documentSize = String.valueOf(fileLengthInKb);

}

private void updateTabId(NegotiationDocumentType type) {

if (type == null) {

this.tabId = NegotiationDocumentType.CORRESPONDENCE_TYPE_ID;

} else {

this.tabId = type.getTabId();

}

}

public String getCurrentValidationState() {

return currentValidationState;

}

public void setCurrentValidationState(String currentValidationState) {

if (currentValidationState == null && isStandard != null) {

this.currentValidationState = DocumentClass.INITIAL_STATE;

} else {

this.currentValidationState = currentValidationState;

}

}

public List<Transition> getAvailableTriggersActions() {

return documentClass.getTransitionFor();

}

public void requestFinalApproval() {

documentClass.requestFinalApproval();

}

public void rejectForNegotiation() {

documentClass.rejectForNegotiation();

}

public void requestApprove() {

documentClass.requestApprove();

}

public void requestForCommentConsent() {

documentClass.requestForCommentConsent();

}

public Boolean isMailSent() {

return mailSent;

}

public void setMailSent(Boolean mailSent) {

this.mailSent = mailSent;

}

public Boolean isCollateralDocumnetType(){

if(this.getDocumentCatagory() != null){return  AddCorrespondence.getColateralDocumentCatagory().contains(this.getDocumentCatagory().getId().toString());

}else{

return Boolean.FALSE;

}

}

public String getDocumentComment() {

return documentComment;

}

public void setDocumentComment(String documentComment) {

this.documentComment = documentComment;

}

public Long getDocumentGroupId(){

return documentGroupId;

}

public void setDocumentGroupId(Long documentGroupId){

this.documentGroupId = documentGroupId;

}

public String getTabId(){

return this.tabId;

}

@Override

public int hashCode() {

final int prime = 31;

int result = 1;

result = prime * result + ((id == null) ? 0 : id.hashCode());

return result;

}

@Override

public boolean equals(Object obj) {

if (this == obj)

return true;

if (obj == null)

return false;

if (getClass() != obj.getClass())

return false;

NegotiationDocument other = (NegotiationDocument) obj;

if (id == null) {

if (other.id != null)

return false;

} else if (!id.equals(other.id)) {

return false;

}

return true;

}

public NegoDocEmailSaved getDocEmailSent() {

return docEmailSent;

}

public void setDocEmailSent(NegoDocEmailSaved docEmailSent) {

this.docEmailSent = docEmailSent;

}

public String getSegmentId() {

return segmentId;

}

public void setSegmentId(String segmentId) {

this.segmentId = segmentId;

}

public Location getCmoLocation() {

return cmoLocation;

}

public void setCmoLocation(Location cmoLocation) {

this.cmoLocation = cmoLocation;

}

@Override

public String getEntityName() {

return BeagleStringUtils.isY(forGroup) ? FOR_GROUP : FOR_AGREEMENT;

}

public boolean isDocPresentForCollOps() {

return isDocPresentForCollOps;

}

public void setDocPresentForCollOps(boolean docPresentForCollOps) {

isDocPresentForCollOps = docPresentForCollOps;

}

}