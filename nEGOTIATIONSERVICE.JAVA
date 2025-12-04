This is NegotiationService.java

package com.bnpparibas.beagle.documents;

import java.io.IOException;

import java.sql.Timestamp;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

import com.bnpparibas.beagle.bulkupload.executeddocument.modal.DocStoreUploadRequest;

import com.bnpparibas.beagle.bulkupload.executeddocument.services.DocStoreAPIService;

import com.bnpparibas.beagle.documents.ui.NegotiationDocumentForExecDocDisplay;

import com.bnpparibas.beagle.kernel.logging.BeagleLogger;

import com.bnpparibas.beagle.kernel.operations.OperationFailed;

import com.bnpparibas.beagle.kernel.operations.interception.CustomeExceptionOperation;

import com.bnpparibas.beagle.ma.model.*;

import com.bnpparibas.beagle.staticdata.model.*;

import org.apache.commons.io.FileUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.StringEscapeUtils;

import com.bnpparibas.beagle.archive.model.ReplacedArchivingDocument;

import com.bnpparibas.beagle.documents.actions.AddCorrespondence;import com.bnpparibas.beagle.documents.model.Attachment;

import com.bnpparibas.beagle.documents.model.CorrespondenceSharedDocument;

import com.bnpparibas.beagle.documents.model.DocumentContainer;

import com.bnpparibas.beagle.documents.model.DocumentContainerForExecDoc;

import com.bnpparibas.beagle.documents.model.DocumentTypeFilter;

import com.bnpparibas.beagle.documents.model.ExecDocTypeFilter;

import com.bnpparibas.beagle.documents.model.ReplacedAttachment;

import com.bnpparibas.beagle.documents.model.SharedArchivingDocument;

import com.bnpparibas.beagle.documents.operations.DocumentParameters;

import com.bnpparibas.beagle.groups.model.Group;

import com.bnpparibas.beagle.groups.model.SelectedMAGroup;

import com.bnpparibas.beagle.kernel.database.BeagleRepository;

import com.bnpparibas.beagle.kernel.services.BeagleFile;

import com.bnpparibas.beagle.kernel.services.FileSystemAccess;

import com.bnpparibas.beagle.kernel.services.MailService;

import com.bnpparibas.beagle.kernel.util.BeagleDateFormat;

import com.bnpparibas.beagle.kernel.util.BeagleStringUtils;

import com.bnpparibas.beagle.kernel.util.BeagleUtils;

import com.bnpparibas.beagle.kernel.util.ListBuilder;

import com.bnpparibas.beagle.staticdata.model.entity.Entity;

import org.springframework.beans.factory.annotation.Value;

@SuppressWarnings({"squid:S3776","squid:S00107"})

public class NegotiationService {

private BeagleRepository repository;

private FileSystemAccess fileSystemAccess;

private MailService mailService;

private CollateralOpsService collateralOpsService;

private static final String APPROVED = "Approved";

private static final String REJECTED_FOR_RENEGOTIATION = "Rejected";

private static final String REJECTED_BY_CMO = "Rejected";

private static final String SENT_FOR_FINAL_APPROVAL = "Sent for final approval";

private static final String SENT_FOR_COMMENT_CONSENT = "Sent for Comment/Consent";

private static final String APPROVE = "Approval Request";

private static final String COMMENT_CONSENT = "Comment/Consent";

private static final String MAIL_TRIGGER = "CSA Workflow";

private DocStoreAPIService docStoreAPIService;

@Value("${attachment.saveTo}")

private String attachmentToBeSavedIn;

public DocStoreAPIService getDocStoreAPIService() {

return docStoreAPIService;

}

public void setDocStoreAPIService(DocStoreAPIService docStoreAPIService) {

this.docStoreAPIService = docStoreAPIService;

}

private String beagleUrl;

private static final BeagleLogger LOG = BeagleLogger.getLog(NegotiationService.class);

public String getBeagleUrl() {

return beagleUrl;

}

public void setBeagleUrl(String beagleUrl) {

this.beagleUrl = beagleUrl;

}

public List addNegotiationItemToGroup(Group group, NegotiationDocumentType documentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, List attachments, User user, NegotiationDocumentType documentCatagory, Boolean standard, String comment, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

repository.findOrCreateNegotiationForGroup(group);

return addNegotiationItem(group, NegotiationDocument.FOR_GROUP, documentType, negotiator, attachments, summary, detail, direction, scope, user, documentDate, documentCatagory, standard, comment, p, null);

}

public List addNegotiationItemToAgreement(MasterAgreement masterAgreement, NegotiationDocumentType documentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, List attachments, User user, NegotiationDocumentType documentCatagory, Boolean standard, String comment, DocumentParameters p, String draftUser) throws IOException,CustomeExceptionOperation,OperationFailed {

repository.findOrCreateNegotiationForAgreement(masterAgreement);

return addNegotiationItem(masterAgreement, NegotiationDocument.FOR_AGREEMENT, documentType, negotiator, attachments, summary, detail, direction, scope, user, documentDate, documentCatagory, standard, comment, p,draftUser);

}

public List addNegotiationItemToAgreementForExecDoc(MasterAgreement masterAgreement, NegotiationDocumentType documentType, Negotiator negotiator, NegotiationDocumentSummaries documentSummary, String summary, String detail, Date documentDate, List attachments, User user, NegotiationDocumentCategories documentCatagory, DocumentParameters p) throws IOException, CustomeExceptionOperation,OperationFailed {

return addNegotiationItemForExecDoc(masterAgreement, "NegotiationDocumentForExecDoc", documentType, negotiator, attachments, documentSummary, summary, detail, user, documentDate, documentCatagory, p);

}

public List addNegotiationItemToAgreementForExecDocForGRP(Group group, NegotiationDocumentType documentType, Negotiator negotiator, NegotiationDocumentSummaries documentSummary, String summary, String detail, Date documentDate, List attachments, User user, NegotiationDocumentCategories documentCatagory, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

return addNegotiationItemForExecDoc(group, "NegotiationDocumentForExecDocForGRP", documentType, negotiator, attachments, documentSummary, summary, detail, user, documentDate, documentCatagory, p);

}

public void updateNegotiationItemForAgreement(List documents, NegotiationDocumentType documentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, List attachments, List documentsToDelete, User user, NegotiationDocumentType documentCatagory, Boolean isStandard, String collateralDocumentNegoValidationTrigger, Boolean documentClassReset, String comment, DocumentParameters p) throws IOException {

NegotiationDocument existingDocument = (NegotiationDocument) documents.get(0);

DocumentContainer container = MasterAgreement.get(repository, existingDocument.getContainerId());

updateNegotiationItem(documents, documentType, negotiator, summary, detail, documentDate, direction, scope, attachments, documentsToDelete, container, user, documentCatagory, isStandard, collateralDocumentNegoValidationTrigger, documentClassReset, comment, p);

}

public void updateNegotiationItemForGroup(List documents, NegotiationDocumentType documentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, List attachments, List documentsToDelete, User user, NegotiationDocumentType documentCatagory, Boolean isStandard, String collateralDocumentNegoValidationTrigger, Boolean documentClassReset, String comment, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

NegotiationDocument existingDocument = (NegotiationDocument) documents.get(0);

DocumentContainer container = Group.get(repository, existingDocument.getContainerId());

updateNegotiationItem(documents, documentType, negotiator, summary, detail, documentDate, direction, scope, attachments, documentsToDelete, container, user, documentCatagory, isStandard, collateralDocumentNegoValidationTrigger, documentClassReset, comment, p);

}

public void updateNegotiationItemForExecDoc(DocumentParameters p, User user) throws IOException,CustomeExceptionOperation,OperationFailed {

List fileIdsToDelete = new ArrayList(p.deleteFilesIds);

fileIdsToDelete.addAll(p.replaceFileIds);

List documentsToDelete = fetchAllNegoDocuments(p,fileIdsToDelete);

updateNegotiationItemExecDoc(p, documentsToDelete, user);

}

public void updateNewNegotiationItemAddedInSharedDocumentRow(List documents, List attachments, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

Map map = new HashMap();

DocumentContainer container;

CorrespondenceSharedDocument existingDocument = (CorrespondenceSharedDocument) documents.get(0);

if (existingDocument.isForGroup()) {

repository.findOrCreateNegotiationForGroup(p.ma().getGroup());

container = Group.get(repository, existingDocument.getContainerId());

} else {

repository.findOrCreateNegotiationForAgreement(p.ma());

container = MasterAgreement.get(repository, existingDocument.getContainerId());

}

addNewAttachmentToNegotiationDocument(documents, p.attachments, container, p, map);

saveAttachments(attachments, map);

}

public List fetchNegotiationDocuments(List documentIds, Long containerId, boolean isForGroup) {

List documents = new ArrayList();

for (Iterator iterator = documentIds.iterator(); iterator.hasNext(); ) {

documents.add(repository.findSharedOrNonSharedNegotiationDocument((Long) iterator.next(), containerId, isForGroup));

}

return documents;

}

private SharedArchivingDocument fetchSharedArchivingDocument(String documentId, MasterAgreement masterAgreement ,DocumentParameters parameters){

SharedArchivingDocument sharedArchivingDocument = null;sharedArchivingDocument = repository.getSharedArchivingDocumentObjofMA(documentId, masterAgreement.getId());

if (sharedArchivingDocument == null && parameters.masterAgreement.belongsToGroup()) {

sharedArchivingDocument = repository.getSharedArchivingDocumentObjofGrp(documentId, masterAgreement.getGroup().getId());

}

return sharedArchivingDocument;

}

private List fetchAllNegoDocuments(DocumentParameters parameters,List fileIds){

List documents = new ArrayList();

for (Iterator i = fileIds.iterator(); i.hasNext(); ) {

Long docId = (Long) i.next();

SharedArchivingDocument sharedArchivingDocument = fetchSharedArchivingDocument(String.valueOf(docId),parameters.ma(),parameters);

if(sharedArchivingDocument != null){

documents.add(NegotiationDocumentForExecDocDisplay.getExecDocObjForSharedDoc(

sharedArchivingDocument ,parameters.masterAgreement));

}else{

NegotiationDocumentForExecDoc negotiationDocumentForExecDoc = repository.findNegotiationDocumentForExecDoc(docId, parameters.isDocumentForGroup);if(negotiationDocumentForExecDoc != null){

documents.add(negotiationDocumentForExecDoc);

}

}

}

return documents;

}

private void updateNegotiationItemExecDoc(DocumentParameters p, List documentsToBeDeleted, User user) throws IOException,CustomeExceptionOperation,OperationFailed {

Map map = new HashMap();

Set<SelectedMAGroup> selectedMAGroups = new HashSet<>();

SharedArchivingDocument sharedArchivingDocument;

for (Iterator i = p.documents.iterator(); i.hasNext(); ) {

NegotiationDocumentForExecDoc document = (NegotiationDocumentForExecDoc) i.next();

if (repository.isDocIdShared(document.getId().toString())) {

sharedArchivingDocument = repository.getSharedArchivingDocumentObjofMA(document.getId().toString(), p.masterAgreement.getId());

if (sharedArchivingDocument == null && p.masterAgreement.belongsToGroup()) {

sharedArchivingDocument = repository.getSharedArchivingDocumentObjofGrp(document.getId().toString(), p.masterAgreement.getGroup().getId());

}

if (sharedArchivingDocument != null && checkToUpdateNegoOrSharedObj(sharedArchivingDocument, p.masterAgreement)) {

sharedArchivingDocument.setNegotiator(p.negotiator);

sharedArchivingDocument.setDetail(p.detail);

sharedArchivingDocument.setDocumentDate(p.documentDate);

sharedArchivingDocument.setAmendmentId(p.amendmentNumber);

if (StringUtils.isNotBlank(p.summary)) {

sharedArchivingDocument.setSummary(p.summary);

} else {

sharedArchivingDocument.setSummary(p.collateralDocSummary);

}

sharedArchivingDocument.setType(p.documentType);

sharedArchivingDocument.setUploaderDetails(user);

sharedArchivingDocument.setDocumentCategories(p.documentCategories);

sharedArchivingDocument.setDocumentSummary(p.documentSummary);

sharedArchivingDocument.setDocumentComment(p.documentComment);

repository.saveOrupdate(sharedArchivingDocument);

if(document.getSharedArchivingDocument() != null){

setNegotiationDocumentExecDocProperties(document, p.documentType, p.negotiator, StringUtils.isNotBlank(p.summary) ? p.summary : p.collateralDocSummary, p.detail, p.documentDate, user, p.documentCatagory, p.documentComment, p.amendmentNumber, p.documentSummary, p.documentCategories);

}

} else {

setNegotiationDocumentExecDocProperties(document, p.documentType, p.negotiator, StringUtils.isNotBlank(p.summary) ? p.summary : p.collateralDocSummary, p.detail, p.documentDate, user, p.documentCatagory, p.documentComment, p.amendmentNumber, p.documentSummary, p.documentCategories);

}

} else {

setNegotiationDocumentExecDocProperties(document, p.documentType, p.negotiator, StringUtils.isNotBlank(p.summary) ? p.summary : p.collateralDocSummary, p.detail, p.documentDate, user, p.documentCatagory, p.documentComment, p.amendmentNumber, p.documentSummary, p.documentCategories);

}

}

if (!p.attachments.isEmpty() || !p.replacedAttachments.isEmpty()) {

NegotiationDocumentForExecDoc existingDocument = (NegotiationDocumentForExecDoc) p.documents.get(0);

DocumentContainerForExecDoc container;

if (p.isDocumentForGroup) {

container = p.ma().getGroup();

} else {

container = p.ma();

}

List allAttachments = new ArrayList();

allAttachments.addAll(p.attachments);

allAttachments.addAll(p.replacedAttachments);

List newDocumentIds = new ArrayList();

for (Iterator i = allAttachments.iterator(); i.hasNext(); ) {

Attachment attachment = (Attachment) i.next();

int size = attachment.getFileSize();

NegotiationDocumentForExecDoc newDocument = createAndSaveNegotiationDocumentForExecDoc(existingDocument, attachment, size, container, p.documentComment, p);

if (p.isDocumentForSelectedMA) {

newDocument.setSelectedMAGrpId(existingDocument.getSelectedMAGrpId());

for (MasterAgreement ma : p.selectedMasters) {

selectedMAGroups.add(new SelectedMAGroup(p.selectedMAId, ma, (Group) container));

}

((Group) container).getSelectedMAGroups().addAll(selectedMAGroups);

}p.documents.add(newDocument);

newDocumentIds.add(newDocument.getId());

map.put(attachment, newDocument);

}

if(Boolean.TRUE.equals(p.throughPopUp)){

if(p.isDocumentForGroup){

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getGroup().getId(), user.getFullname(), newDocumentIds.toString());

}else{

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getId(), user.getFullname(), newDocumentIds.toString());

}

}

}

List documentsToBeReplaced = fetchAllNegoDocuments(p,p.replaceFileIds);

for (Iterator i = documentsToBeReplaced.iterator(); i.hasNext(); ) {

NegotiationDocumentForExecDoc document = (NegotiationDocumentForExecDoc) i.next();

List<Long> newIds = new ArrayList<>();

for (Object attachment : p.replacedAttachments) {

if (((ReplacedAttachment) attachment).getReplacedFileId().equals(document.getId())) {

newIds.add(((NegotiationDocumentForExecDoc) map.get(attachment)).getId());

}

}

saveReplacedArchDocObjects(document, newIds); // To maintain the old archiving document

}

for (Iterator i = documentsToBeDeleted.iterator(); i.hasNext(); ) {

NegotiationDocumentForExecDoc document = (NegotiationDocumentForExecDoc) i.next();

if (p.documents.size() > 1) {

p.documents.remove(document);

if(document.getSharedArchivingDocument() != null){

repository.delete(document.getSharedArchivingDocument());

}else{

repository.delete(document);

}

} else {

document.removeFile();

}

/*Document Replacement functionality update the archiving id in deleted Document*/

/*if(p.replaceFileIds.contains(document.getId())){

document.getArchiveTask().setArchiveId(replacedDocumentMap.get(document.getId()).getId().toString());

repository.saveOrupdate(document.getArchiveTask());

repository.getSession().flush();

repository.getSession().refresh(document.getArchiveTask());

updatedArchivingTasks.add(document.getArchiveTask());

}*/

}if(Boolean.TRUE.equals(p.throughPopUp)){

if(!p.deleteFilesIds.isEmpty()){

if(p.isDocumentForGroup){

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getGroup().getId(), user.getFullname(), p.deleteFilesIds.toString());

}else{

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getId(), user.getFullname(), p.deleteFilesIds.toString());

}

}

}

// Perform database action first - because thats transactional.

// Only if that succeeds, do we do the filesystem stuff (because thats harder to "rollback"

saveAttachmentsForExecDoc(p.attachments, map);

saveAttachmentsForExecDoc(p.replacedAttachments, map);

if (p.documentType.getId().equals(NegotiationDocumentType.TRIPARTY_TYPE)) {

sendEmailForSpecificDocumentType(p, p.documents, map, "update");

}

return;

}

private static Boolean checkToUpdateNegoOrSharedObj(SharedArchivingDocument sharedArchivingDocument, MasterAgreement masterAgreement) {

Boolean flag;

if (masterAgreement.belongsToGroup()) {

if (sharedArchivingDocument.getOriginalGroupId() != null) {

if (sharedArchivingDocument.getOriginalGroupId().compareTo(masterAgreement.getGroup().getId()) != 0) {

flag = true;

} else {

flag = false;

}

} else {

if (sharedArchivingDocument.getOriginalMAId().compareTo(masterAgreement.getId()) != 0) {

flag = true;

} else {

flag = false;

}

}

} else {

if (sharedArchivingDocument.getOriginalMAId() != null) {

if (sharedArchivingDocument.getOriginalMAId().compareTo(masterAgreement.getId()) != 0) {

flag = true;

} else {

flag = false;

}

} else {

if (sharedArchivingDocument.getOriginalGroupId().compareTo(masterAgreement.getId()) != 0) {

flag = true;

} else {

flag = false;

}

}

}

return flag;

}

private void saveReplacedArchDocObjects(NegotiationDocumentForExecDoc document, List<Long> newIds) {

ReplacedArchivingDocument replacedArchDoc;

/*

* To find in replaced documents where new_archiving id = archiving id* of document passed if yes then update the existing document else

* create the new document

*/

// finding existing replaced doucment

/*		Criteria criteria = repository.getSession().createCriteria(ReplacedArchivingDocument.class)	.add(Restrictions.eq("newArchivingId", document.getId()));

Object replacedObj = criteria.uniqueResult();

if (replacedObj != null) {

replacedArchDoc = (ReplacedArchivingDocument) replacedObj;

replacedArchDoc.setNewArchivingId(newDoc.getId());

// document is alreday replaced once, multiple replace condition

} else {

replacedArchDoc = new ReplacedArchivingDocument();

replacedArchDoc.setId(document.getId());

replacedArchDoc.setContainerId(document.getContainerId());

replacedArchDoc.setAmendmentId(document.getAmendmentId());

replacedArchDoc.setDetail(document.getDetail());

replacedArchDoc.setDocumentCategory(document.getDocumentCategory());

replacedArchDoc.setDocumentComment(document.getDocumentComment());

replacedArchDoc.setDocumentDate(document.getDocumentDate());

replacedArchDoc.setDocumentGroupId(document.getDocumentGroupId());

replacedArchDoc.setNegotiator(document.getNegotiator());

replacedArchDoc.setSummary(document.getSummary());

replacedArchDoc.setType(document.getType());

replacedArchDoc.setUploaderDetails(document.getUploader());

replacedArchDoc.setUploaderUid(document.getUploaderUid());

replacedArchDoc.setCreation(document.getCreationUser(),	document.getCreationDate());

replacedArchDoc.setNewArchivingId(newDoc.getId());

replacedArchDoc.setFileName(document.getFileName());

replacedArchDoc.setDocumentCategory(document.getDocumentCategory());

repository.save("ReplacedArchivingDocument", replacedArchDoc);

}*/

for (Long newId : newIds) {

replacedArchDoc = new ReplacedArchivingDocument();

replacedArchDoc.setArchivingId(document.getId());

if (document.isForGroup()) {

replacedArchDoc.setGrpId(document.getContainerId());

} else {

replacedArchDoc.setMaId(document.getContainerId());

}

replacedArchDoc.setAmendmentId(document.getAmendmentId());

replacedArchDoc.setDetail(document.getDetail());

replacedArchDoc.setDocumentCategory(document.getDocumentCategory());

replacedArchDoc.setDocumentComment(document.getDocumentComment());

replacedArchDoc.setDocumentDate(document.getDocumentDate());

replacedArchDoc.setDocumentGroupId(document.getDocumentGroupId());

replacedArchDoc.setNegotiator(document.getNegotiator());

replacedArchDoc.setSummary(document.getSummary());

replacedArchDoc.setType(document.getType());

replacedArchDoc.setUploaderDetails(document.getUploader());

replacedArchDoc.setUploaderUid(document.getUploaderUid());

replacedArchDoc.setCreation(document.getCreationUser(), document.getCreationDate());

replacedArchDoc.setNewArchivingId(newId);

replacedArchDoc.setFileName(document.getFileName());

replacedArchDoc.setDocumentCategory(document.getDocumentCategory());

replacedArchDoc.setOldDocStoreId(document.getDocStoreFileId());

repository.save("ReplacedArchivingDocument", replacedArchDoc);

}

}

public void updateCorrespondenceSharedDocument(List documents, NegotiationDocumentType negotiationDocumentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, User user, NegotiationDocumentType documentCatagory, Boolean isStandard, String collateralDocumentNegoValidationTrigger, Boolean documentClassReset, String comment, DocumentParameters p) throws IOException {

//Update the correspondence shared document entry only

for (Iterator i = documents.iterator(); i.hasNext(); ) {

CorrespondenceSharedDocument document = (CorrespondenceSharedDocument) i.next();

setNegotiationDocumentProperties(document, negotiationDocumentType, negotiator, summary, detail, documentDate, direction, scope, user, documentCatagory, comment, p.documentComment, p.cmoLocation);

setNegotiationCollateralValidationState(document, collateralDocumentNegoValidationTrigger, isStandard, documentClassReset, p);

document.setSegmentId(p.segmentId);

}

}private void updateNegotiationItem(List documents, NegotiationDocumentType negotiationDocumentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, List attachments, List documentsToDelete, DocumentContainer container, User user, NegotiationDocumentType documentCatagory, Boolean isStandard, String collateralDocumentNegoValidationTrigger, Boolean documentClassReset, String comment, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

Map map = new HashMap();

boolean collateralDocStatusAudit = collateralOpsService.isRowWillBeAddedForCollateralDocStatusAudit((NegotiationDocument) documents.get(0), p, attachments.size(), documentsToDelete.size());

updateNegotiationDocument(documents, negotiationDocumentType, negotiator, summary, detail, documentDate, direction, scope, user, documentCatagory, isStandard, collateralDocumentNegoValidationTrigger, documentClassReset, comment, p);

addNewAttachmentToNegotiationDocument(documents, attachments, container, p, map);

collateralOpsService.updateCollateralDocDetails(documents, p, user, collateralDocStatusAudit);

// Perform database action first - because thats transactional.

// Only if that succeeds, do we do the filesystem stuff (because thats harder to "rollback"

saveAttachments(attachments, map);

if (!documents.isEmpty()) {

NegotiationDocument negotiationDocument = (NegotiationDocument) documents.get(0);

sendMailWithStatus(p, negotiationDocument, p.collateralDocumentNegoValidationTrigger, documents);

}

}

public void deleteAttachmentFromNegotiationDocument(List documents, List documentsToDelete) {

for (Iterator i = documentsToDelete.iterator(); i.hasNext(); ) {

NegotiationDocument document = (NegotiationDocument) i.next();

if (documents.size() > 1) {

documents.remove(document);

repository.delete(document.getEntityName(), document);

} else {

document.removeFile();

}

}

}private void addNewAttachmentToNegotiationDocument(List documents, List attachments, DocumentContainer container, DocumentParameters p, Map map) throws IOException {

if (!attachments.isEmpty()) {

NegotiationDocument existingDocument = (NegotiationDocument) documents.get(0);

for (Iterator i = attachments.iterator(); i.hasNext(); ) {

Attachment attachment = (Attachment) i.next();

int size = attachment.getFileSize();

NegotiationDocument newDocument = createAndSaveNegotiationDocument(existingDocument, attachment, size, container, documents, p.documentComment);

map.put(attachment, newDocument);

}

}

}

public void updateNegotiationDocument(List documents, NegotiationDocumentType negotiationDocumentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, User user, NegotiationDocumentType documentCatagory, Boolean isStandard, String collateralDocumentNegoValidationTrigger, Boolean documentClassReset, String comment, DocumentParameters p) {for (Iterator i = documents.iterator(); i.hasNext(); ) {

NegotiationDocument document = (NegotiationDocument) i.next();

setNegotiationDocumentProperties(document, negotiationDocumentType, negotiator, summary, detail, documentDate, direction, scope, user, documentCatagory, comment, p.documentComment, p.cmoLocation);

setNegotiationCollateralValidationState(document, collateralDocumentNegoValidationTrigger, isStandard, documentClassReset, p);

}

}

private void sendMailWithStatus(DocumentParameters p, NegotiationDocument document, String collateralDocumentNegoValidationTrigger, List documents) {

if (document.getIsStandard() != null) {

if (document.getIsStandard()) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_FINAL_APPROVE, documents, null, document.getUploader());

}if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO, documents, null, document.getUploader());

}

if (StandardNegoDocument.TRIGGER_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_APPROVE, documents, null, document.getUploader());

}

} else if (!document.getIsStandard()) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_FINAL_APPROVE, documents, null, document.getUploader());

}

if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO, documents, null, document.getUploader());

}

if (StandardNegoDocument.TRIGGER_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_APPROVE, documents, null, document.getUploader());

}

if (StandardNegoDocument.TRIGGER_FOR_COMMENT_CONSENT.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

sendMail(p.ma(), p.amendment, document, StandardNegoDocument.TRIGGER_FOR_COMMENT_CONSENT, documents, null, document.getUploader());

}

}

}

}

public void deleteNegotiationItem(List documents, boolean forGroup, User user) {

for (Iterator i = documents.iterator(); i.hasNext(); ) {

NegotiationDocument document = (NegotiationDocument) i.next();

repository.delete(document.getEntityName(), document);

}

if (!forGroup) {

collateralOpsService.deleteCollateralDocDetails(documents, user);

}

// Perform database action first - because thats transactional.

// Only if that succeeds, do we do the filesystem stuff (because thats harder to "rollback")

}

public void deleteNegotiationItemForExec(List documents) {

for (Iterator i = documents.iterator(); i.hasNext(); ) {

NegotiationDocumentForExecDoc document = (NegotiationDocumentForExecDoc) i.next();

repository.delete("NegotiationDocumentForExecDoc", document);

}

}

public void deleteSharedNegotiationItemForExec(List documents) {

for (Iterator i = documents.iterator(); i.hasNext(); ) {

SharedArchivingDocument document = (SharedArchivingDocument) i.next();

repository.delete("SharedArchivingDocument", document);

}

}

private NegotiationDocument createAndSaveNegotiationDocument(NegotiationDocument existingDocument, Attachment attachment, int size, DocumentContainer container, List documents, String documentComment) {

NegotiationDocument newDocument;

newDocument = new NegotiationDocument(

existingDocument.getContainerId(),

existingDocument.getType(),

existingDocument.getNegotiator(),

existingDocument.getUploader(),

existingDocument.getDocumentDate(),

existingDocument.getSummary(),

existingDocument.getDetail(),

existingDocument.getDirection(),

existingDocument.getScope(),

attachment.getFilename(),

size,

existingDocument.isForGroup(),

existingDocument.getDocumentCatagory(), existingDocument.getIsStandard(), existingDocument.getEmailNotificationsComments(), documentComment, existingDocument.getDocumentGroupId(), existingDocument.getSegmentId(), existingDocument.getCmoLocation());

if (NegotiationDocument.FOR_AGREEMENT.equals(newDocument.getEntityName()) && newDocument.getDocumentGroupId() == null) {

newDocument.setDocumentGroupId(repository.getNextNegotiationDocGroupNumber());

}

newDocument.setCurrentValidationState(existingDocument.getCurrentValidationState());

container.addNegotiationDocument(newDocument);

documents.add(newDocument);

repository.save(newDocument.getEntityName(), newDocument);

return newDocument;

}

private NegotiationDocumentForExecDoc createAndSaveNegotiationDocumentForExecDoc(NegotiationDocumentForExecDoc existingDocument, Attachment attachment, int size, DocumentContainerForExecDoc container, String documentComment, DocumentParameters p) {

NegotiationDocumentForExecDoc newDocument;

if (StringUtils.isBlank(existingDocument.getFileName())) {

existingDocument.setFileName(attachment.getFilename());

existingDocument.setDocumentSize(size);

newDocument = existingDocument;

} else {

newDocument = new NegotiationDocumentForExecDoc(

p.isDocumentForGroup ? p.amendment.getMasterAgreement().getGroup() : p.ma(),

existingDocument.getType(),

existingDocument.getNegotiator(),

existingDocument.getUploader(),

existingDocument.getDocumentDate(),

existingDocument.getDocumentSummary(),

existingDocument.getSummary(),

existingDocument.getDetail(),

attachment.getFilename(),

size,

existingDocument.isForGroup(), existingDocument.getDocumentCategories(), documentComment, existingDocument.getDocumentGroupId(), existingDocument.getAmendmentId(), p.isDocumentForSelectedMA);

if (p.isDocumentForGroup) {

((Group) container).getNegotiationDocumentsForExecDocs().add(newDocument);

} else {

((MasterAgreement) container).addNegotiationDocumentExecDoc(newDocument);

}

if (p.isDocumentForGroup) {

repository.save("NegotiationDocumentForExecDocForGRP", newDocument);

} else {

repository.save("NegotiationDocumentForExecDoc", newDocument);

}

}

return newDocument;

}

public void updateNegotiationDetails(MasterAgreement masterAgreement, Negotiation copyMe) {

String[] specificProperties = {

"priority",

"outstandingAncillaryDocuments",

"creditLine",

"creditQuestionnaireReceived",

"exposure"

};

String[] groupProperties = {

"nextChaserDate",

"firstDraftSent",

"chaserDeadline",

};

List agreements = repository.findAllAgreementsInSameGroup(masterAgreement.getId());

for (Iterator i = agreements.iterator(); i.hasNext(); ) {

MasterAgreement ma = (MasterAgreement) i.next();

Negotiation negotiation = repository.findOrCreateNegotiationForAgreement(ma);

BeagleUtils.copyProperties(copyMe, negotiation, ListBuilder.build(groupProperties));

if (masterAgreement.equals(ma)) {

BeagleUtils.copyProperties(copyMe, negotiation, ListBuilder.build(specificProperties));

}

ma.getLastAmendment().forceFlush();

}

if (masterAgreement.getGroup() != null) {

Negotiation negotiation = repository.findOrCreateNegotiationForGroup(masterAgreement.getGroup());

BeagleUtils.copyProperties(copyMe, negotiation, ListBuilder.build(groupProperties));

}

}

private static void setNegotiationDocumentProperties(NegotiationDocument document, NegotiationDocumentType negotiationDocumentType, Negotiator negotiator, String summary, String detail, Date documentDate, String direction, String scope, User user, NegotiationDocumentType documentCatagory, String comment, String documentComment, Location cmoLocation) {

document.setType(negotiationDocumentType);

document.setNegotiator(negotiator);

document.setSummary(summary);

document.setDetail(detail);

document.setDocumentDate(documentDate);

document.setUploader(user);

document.setDirection(direction);

document.setScope(scope);

document.setDocumentCatagory(documentCatagory);

document.setEmailNotificationsComments(comment);

document.setDocumentComment(documentComment);

document.setCmoLocation(cmoLocation);

}

private static void setNegotiationDocumentExecDocProperties(NegotiationDocumentForExecDoc document, NegotiationDocumentType negotiationDocumentType, Negotiator negotiator, String summary, String detail, Date documentDate, User user, NegotiationDocumentType documentCatagory, String documentComment, Long amendmentNumber, NegotiationDocumentSummaries documentSummary, NegotiationDocumentCategories categories) {

if (documentComment != null) {

documentComment = StringEscapeUtils.unescapeHtml(documentComment);

}

document.setType(negotiationDocumentType);document.setNegotiator(negotiator);

document.setSummary(summary);

document.setDocumentSummary(documentSummary);

document.setDetail(detail);

document.setDocumentDate(documentDate);

document.setUploader(user);

document.setDocumentCategories(categories);

document.setDocumentCategory(documentCatagory);

document.setDocumentComment(documentComment);

document.setAmendmentId(amendmentNumber);

}

private List addNegotiationItem(DocumentContainer container, String entityName, NegotiationDocumentType documentType, Negotiator negotiator, List attachments, String summary, String detail, String direction, String scope, User user, Date documentDate, NegotiationDocumentType documentCatagory, Boolean standard, String comment, DocumentParameters p, String drafterUid) throws IOException,CustomeExceptionOperation,OperationFailed {

List newIds = new ArrayList();

List documents = new ArrayList();

String detailFieldWithTimeStamp;

Long documentGroupId = Long.valueOf(0);

StringBuilder fileName = new StringBuilder();if (StringUtils.equals(NegotiationDocument.FOR_AGREEMENT, entityName)) {

documentGroupId = repository.getNextNegotiationDocGroupNumber();

}

if (documentCatagory != null && AddCorrespondence.getColateralDocumentCatagory().contains(documentCatagory.getId().toString())) {

StringBuilder modifiedDetailField = new StringBuilder("Added On : " + BeagleDateFormat.format(new Date(), BeagleDateFormat.FORMAT_WITH_TIME) + (BeagleStringUtils.isEmpty(detail) ? " " : "\n"));

modifiedDetailField.append(detail);

detailFieldWithTimeStamp = modifiedDetailField.toString();

} else {

detailFieldWithTimeStamp = detail;

}

if (attachments.isEmpty()) {

NegotiationDocument negotiationDocument = new NegotiationDocument(container.getId(), documentType, negotiator, user, documentDate, summary, detailFieldWithTimeStamp, direction, scope, null, 0, container instanceof Group, documentCatagory, standard, comment, p.documentComment, documentGroupId, p.segmentId, p.cmoLocation);

negotiationDocument.setCurrentValidationState(p.collateralDocumentNegoValidationStatus);

if (shouldLoadDataFromPreviousCollateralEntry(p, container.getId())) {

if (!(getPreviousCollateralEntry(p, container.getId()).getIsStandard().equals(p.isStandard))) {

p.documentClassReset = Boolean.TRUE;

}

}

setNegotiationCollateralValidationState(negotiationDocument, p.collateralDocumentNegoValidationTrigger, p.isStandard, p.documentClassReset, p);

container.addNegotiationDocument(negotiationDocument);

repository.save(entityName, negotiationDocument);

documents.add(negotiationDocument);

newIds.add(negotiationDocument.getId());

collateralOpsService.saveCollateralDocDeatilsForValidation(negotiationDocument, user, p, fileName.toString());

} else {

Map map = new HashMap();

NegotiationDocument negotiationDocument = null;

int count = 0;

for (Iterator iterator = attachments.iterator(); iterator.hasNext(); ) {

Attachment attachment = (Attachment) iterator.next();

int size = attachment.getFileSize();

negotiationDocument = new NegotiationDocument(container.getId(), documentType, negotiator, user, documentDate, summary, detailFieldWithTimeStamp, direction, scope, attachment.getFilename(), size, container instanceof Group, documentCatagory, standard, comment, p.documentComment, documentGroupId, p.segmentId, p.cmoLocation);

negotiationDocument.setCurrentValidationState(p.collateralDocumentNegoValidationStatus);

if(drafterUid != null){

User drafter = repository.getObject(User.class,drafterUid);

if(drafter != null){

negotiationDocument.setDrafter(drafter);

}

}

if (shouldLoadDataFromPreviousCollateralEntry(p, container.getId())) {

if ( getPreviousCollateralEntry(p, container.getId()).getIsStandard() !=null &&

!(getPreviousCollateralEntry(p, container.getId()).getIsStandard().equals(p.isStandard))) {

p.documentClassReset = Boolean.TRUE;

}

}

setNegotiationCollateralValidationState(negotiationDocument, p.collateralDocumentNegoValidationTrigger, p.isStandard, p.documentClassReset, p);

container.addNegotiationDocument(negotiationDocument);

map.put(attachment, negotiationDocument);

repository.save(entityName, negotiationDocument);

documents.add(negotiationDocument);

newIds.add(negotiationDocument.getId());

if (count > 0) {

fileName.append("<BR>");

}

fileName = fileName.append(attachment.getFilename());

count++;

}

collateralOpsService.saveCollateralDocDeatilsForValidation(negotiationDocument, user, p, fileName.toString());

// Perform database action first - because thats transactional.

// Only if that succeeds, do we do the filesystem stuff (because thats harder to "rollback"

saveAttachments(attachments, map);

}

if (!documents.isEmpty()) {

sendMailWithStatus(p, (NegotiationDocument) documents.get(0), p.collateralDocumentNegoValidationTrigger, documents);

}

return newIds;

}

private List addNegotiationItemForExecDoc(DocumentContainerForExecDoc container, String entityName, NegotiationDocumentType documentType, Negotiator negotiator, List attachments, NegotiationDocumentSummaries documentSummary, String summary, String detail, User user, Date documentDate, NegotiationDocumentCategories documentCatagory, DocumentParameters p) throws IOException,CustomeExceptionOperation,OperationFailed {

List newIds = new ArrayList();

List documents = new ArrayList();

String detailFieldWithTimeStamp;

Long documentGroupId = Long.valueOf(0);

StringBuilder fileName = new StringBuilder();

Long selectedMAId = null;

Set<SelectedMAGroup> selectedMAGroups = new HashSet<SelectedMAGroup>();

Map attachmentMap = new HashMap();

if (StringUtils.equals(NegotiationDocument.FOR_AGREEMENT, entityName)) {

documentGroupId = repository.getNextNegotiationDocGroupNumber();

}

if (p.isDocumentForSelectedMA) {

selectedMAId = repository.getNextSelectedMAGroupNumber();

}

detailFieldWithTimeStamp = detail;

if (attachments.isEmpty()) {

NegotiationDocumentForExecDoc negotiationDocument = new NegotiationDocumentForExecDoc(container, documentType, negotiator, user, documentDate, documentSummary, summary, detailFieldWithTimeStamp, null, 0, p.isDocumentForGroup, documentCatagory, p.documentComment, documentGroupId, p.amendmentNumber, p.isDocumentForSelectedMA);

if (p.isDocumentForSelectedMA) {

for (MasterAgreement ma : p.selectedMasters) {

selectedMAGroups.add(new SelectedMAGroup(selectedMAId, ma, (Group) container));

}

((Group) container).getSelectedMAGroups().addAll(selectedMAGroups);

//negotiationDocument.setSelectedMAGroup(selectedMAGroups);

}

if (null != p.replicateToAllGroupOption) {

Long grpid = p.ma().getGroup().getId();

for (Object ma : repository.findMasterAgreementsByGroup(grpid)) {

MasterAgreement masterAgreement = (MasterAgreement) ma;

}

}

negotiationDocument.setSelectedMAGrpId(selectedMAId);

container.addNegotiationDocumentExecDoc(negotiationDocument);/*if (p.isDocumentForSelectedMA) {

for(SelectedMAGroup maGroup : selectedMAGroups){

repository.save("SelectedMAGroup",maGroup);

}

}*/

repository.save(entityName, negotiationDocument);

documents.add(negotiationDocument);

newIds.add(negotiationDocument.getId());

if(Boolean.TRUE.equals(p.throughPopUp)){

if(p.isDocumentForGroup){

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getGroup().getId(), user.getFullname(), newIds.toString());

}else{

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getId(), user.getFullname(), newIds.toString());

}

}

} else {

NegotiationDocumentForExecDoc negotiationDocument;

int count = 0;

for (Iterator iterator = attachments.iterator(); iterator.hasNext(); ) {

Attachment attachment = (Attachment) iterator.next();

int size = attachment.getFileSize();

negotiationDocument = new NegotiationDocumentForExecDoc(container, documentType, negotiator, user, documentDate, documentSummary, summary, detailFieldWithTimeStamp, attachment.getFilename(), size, p.isDocumentForGroup, documentCatagory, p.documentComment, documentGroupId, p.amendmentNumber, p.isDocumentForSelectedMA);

if (p.isDocumentForSelectedMA) {

for (MasterAgreement ma : p.selectedMasters) {

selectedMAGroups.add(new SelectedMAGroup(selectedMAId, ma, (Group) container));

}

((Group) container).getSelectedMAGroups().addAll(selectedMAGroups);

}

negotiationDocument.setSelectedMAGrpId(selectedMAId);

container.addNegotiationDocumentExecDoc(negotiationDocument);

attachmentMap.put(attachment, negotiationDocument);

/*if (p.isDocumentForSelectedMA) {

for(SelectedMAGroup maGroup : selectedMAGroups){

repository.save("SelectedMAGroup",maGroup);

}

}*/

repository.save(entityName, negotiationDocument);

documents.add(negotiationDocument);

newIds.add(negotiationDocument.getId());

if (count > 0) {

fileName.append("<BR>");

}

fileName = fileName.append(attachment.getFilename());

count++;

}

if(Boolean.TRUE.equals(p.throughPopUp)){

if(p.isDocumentForGroup){

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getGroup().getId(), user.getFullname(), newIds.toString());

}else{

repository.addAuditForConfirmationPopUp(p.isDocumentForGroup,p.masterAgreement.getId(), user.getFullname(), newIds.toString());

}

}

// Perform database action first - because thats transactional.

// Only if that succeeds, do we do the filesystem stuff (because thats harder to "rollback"

saveAttachmentsForExecDoc(attachments, attachmentMap);

}

if (p.documentType.getId().equals(NegotiationDocumentType.TRIPARTY_TYPE)) {

sendEmailForSpecificDocumentType(p, attachments, attachmentMap, "Add");

}

if (null != p.masterAgreement.getGroup()) {

repository.updateNegoDateForGroup(p.masterAgreement.getGroup().getId());

} else {

p.masterAgreement.updateNegotiationDateForMa();

}

return newIds;

}

private void sendEmailForSpecificDocumentType(DocumentParameters p, List attachments, Map attachmentMap, String mode) {

Map<String, List> toAndCcIds = repository.getToMailListForManageParameters(NegotiationDocumentType.TRIPARTY_DOCUMENT_TYPE);

if (!toAndCcIds.isEmpty()) {

MasterAgreement masterAgreement = p.ma();

List<String> ccList = new ArrayList<String>();

StringBuilder body = new StringBuilder();

Entity counterParty;

Entity bnppEntity = masterAgreement.getBnpParibasEntity();

String subject;

subject = "[Triparty Eligibility Sets] - ref : ";

if (p.negotiator != null) {

ccList.add(p.negotiator.getUser().getEmail());

}

if (!masterAgreement.belongsToGroup()) {

subject += (" MA " + masterAgreement.getId().toString());

} else {

subject += (" group " + masterAgreement.getGroup().getId());

}

subject += " - ";

if (masterAgreement.isStandard()) {counterParty = masterAgreement.getCounterpartyEntity();

} else if (masterAgreement.getCounterpartyIsManaged()) {

counterParty = masterAgreement.getActingEntity(Party.COUNTERPARTY);

} else {

counterParty = masterAgreement.getActingEntity(Party.BNP_PARIBAS);

}

subject += counterParty.getCodes() + " " + counterParty.getName();

List<BeagleFile> files = new ArrayList<BeagleFile>();

addAttachments(attachments, attachmentMap, files, mode);

generateEmailBodyForTripartyType(p, masterAgreement, body, counterParty, bnppEntity);

mailService.sendEmailForSpecificDocumentType(toAndCcIds.get("to"), masterAgreement, ccList, subject, body.toString(), files, p.amendment != null ? p.amendment.getAdminLocation().getName() : "");

}

}

private void generateEmailBodyForTripartyType(DocumentParameters p, MasterAgreement masterAgreement, StringBuilder body, Entity counterParty, Entity bnppEntity) {

body.append("Dear All,\n\n");if (!p.selectedMasters.isEmpty() || (p.replicateOption.equals(Boolean.TRUE) && p.replicateToAllGroupOption.equals(Boolean.TRUE))) {

List<MasterAgreement> masterAgreements;

if (p.selectedMasters.isEmpty()) {

masterAgreements = repository.findMasterAgreementsByGroup(masterAgreement.getGroup().getId());

} else {

masterAgreements = p.selectedMasters;

}

StringBuilder maIds = new StringBuilder();

int lineCount = 0;

for (MasterAgreement ma : masterAgreements) {

if (lineCount == 0) {

maIds.append(ma.getId().toString());

} else {

maIds.append(", " + ma.getId().toString());

}

lineCount++;

}

body.append("Please be advised that document related to \"Triparty Eligibility Set\" has been uploaded in Beagle for the below Agreements: " + "" + "\n\n");

body.append("Agreement Numbers : " + maIds + "\n");

} else {

body.append("Please be advised that document related to \"Triparty Eligibility Set\" has been uploaded in Beagle for the below Agreement: " + "" + "\n\n");body.append("Agreement Number : " + masterAgreement.getId() + "\n");

}

if (masterAgreement.belongsToGroup()) {

body.append("Group Number : " + masterAgreement.getGroup().getId() + "\n");

}

body.append("Agreement Type : " + masterAgreement.getAgreementType() + "\n");

body.append("Counterparty " + (masterAgreement.belongsToGroup() ? "Acting " : "") + "Entity Name : " + counterParty.getLeiOrCrdsNameForMail() + "\n");

body.append("Counterparty " + (masterAgreement.belongsToGroup() ? "Acting Entity " : "") + "LEI : " + counterParty.getLeiIdForDisplay() + "\n");

body.append("Counterparty " + (masterAgreement.belongsToGroup() ? "Acting Entity " : "") + "CRDS Code : " + counterParty.getCodes() + "\n");

body.append("BNPPARIBAS Entity Name : " + bnppEntity.getLeiOrCrdsNameForMail() + "\n");

body.append("BNPPARIBAS Entity LEI : " + bnppEntity.getLeiIdForDisplay() + "\n");

body.append("BNPPARIBAS CRDS Code : " + bnppEntity.getCodes() + "\n");

body.append("Negotiator : " + p.negotiator.getFullName() + "\n");

body.append("\nRegards,\n" +

"Beagle Team\n");

}

private void addAttachments(List attachments, Map attachmentMap, List<BeagleFile> files, String mode) {

if (attachments != null && !attachments.isEmpty()) {

for (Iterator iterator = attachments.iterator(); iterator.hasNext(); ) {

NegotiationDocumentForExecDoc negotiationDocument;

if ("Add".equals(mode)) {

Attachment attachment = (Attachment) iterator.next();

negotiationDocument = (NegotiationDocumentForExecDoc) attachmentMap.get(attachment);

} else {

negotiationDocument = (NegotiationDocumentForExecDoc) iterator.next();

}

BeagleFile file = fileSystemAccess.getFile(negotiationDocument.getId().toString(), negotiationDocument.getFileName());

if (!files.contains(file)) {

files.add(file);

}

}

}

}

private void saveAttachments(List attachments, Map map) throws IOException,CustomeExceptionOperation,OperationFailed {

for (Iterator iterator = attachments.iterator(); iterator.hasNext(); ) {Attachment attachment = (Attachment) iterator.next();

NegotiationDocument negotiationDocument = (NegotiationDocument) map.get(attachment);

if(!"DOCSTORE".equals(attachmentToBeSavedIn)) {

fileSystemAccess.save(negotiationDocument.getId().toString(), attachment.getFile());

} else {

byte[] fileContent = FileUtils.readFileToByteArray(attachment.getFile());

DocStoreUploadRequest docStoreUploadRequest = new DocStoreUploadRequest();

docStoreUploadRequest.setName(String.valueOf(negotiationDocument.getId()));

docStoreUploadRequest.setContent(fileContent);

Map<String,String> metaData = new HashMap<>();

metaData.put("FileDisplayName",attachment.getFilename());

metaData.put("Category",negotiationDocument.getDocumentCatagory()!=null ? negotiationDocument.getDocumentCatagory().getText() : "");

metaData.put("Type",negotiationDocument.getType()!=null ? negotiationDocument.getType().getText() : "");

docStoreUploadRequest.setMetadata(metaData);

String docStoreId = docStoreAPIService.uploadDocument(docStoreUploadRequest,attachment.getFile());

negotiationDocument.setDocStoreFileId(docStoreId);

negotiationDocument.setDocumentPresentInDocstore(true);

//repository.update(negotiationDocument);

}

}

}

public void setFileSystemAccess(FileSystemAccess fileService) {

this.fileSystemAccess = fileService;

}

public FileSystemAccess getFileSystemAccess() {

return fileSystemAccess;

}

public BeagleRepository getBeagleRepository() {

return repository;

}

public void setBeagleRepository(BeagleRepository repository) {

this.repository = repository;

}

public CollateralOpsService getCollateralOpsService() {

return collateralOpsService;

}

public void setCollateralOpsService(CollateralOpsService collateralOpsService) {

this.collateralOpsService = collateralOpsService;

}

public static List getDocumentGroups(MasterAgreement ma, DocumentTypeFilter documentTypeFilter, BeagleRepository repository) {List result = new ArrayList();

List docGroups = new ArrayList(ma.getNegotiationDocumentGroups());

Collections.sort(docGroups, new DocumentListComparator());

for (Iterator i = docGroups.iterator(); i.hasNext(); ) {

List documents = (List) i.next();

NegotiationDocument doc = (NegotiationDocument) documents.iterator().next();

NegotiationDocumentType docType = doc.getType();

if (documentTypeFilter.accept(docType)) {

result.add(documents);

}

}

return result;

}

private static final ConcurrentHashMap<String, List<List>> docGroupsCache = new ConcurrentHashMap<>();

private static final Set<Integer> executedAmendmentTypes =  new HashSet<>(NegotiationDocumentForExecDoc.getExecutedDocumentationAmendmentTypes());

public static List<List> getExecutedDocumentGroups(MasterAgreement ma, ExecDocTypeFilter documentTypeFilter, boolean isApplicableDoc, BeagleRepository repository) {

List<List> docGroups =  new ArrayList<>(ma.getNegotiationDocumentExecDocAllGroups(ma,repository));docGroups.sort(new ExecutedDocumentListComparator());

List<List> result = new ArrayList<>();

for(List<NegotiationDocumentForExecDoc> documents : docGroups){

Collections.sort(documents);

NegotiationDocumentForExecDoc doc = (NegotiationDocumentForExecDoc) documents.iterator().next();

NegotiationDocumentType documentType = doc.getType();

NegotiationDocumentCategories categories = doc.getDocumentCategories();

NegotiationDocumentSummaries summaries = doc.getDocumentSummary();

if (!documentTypeFilter.accept(documentType, categories, summaries, ma)) {

continue;

}

if (!isApplicableDoc) {

result.add(documents);

continue;

}

if (!doc.isForGroup() && docHasSameContainerIdAndSameAmendmentId(doc, ma)

&& executedAmendmentTypes.contains(documentType.getId())) {

result.add(documents);

} else if (docIsForGrpAndSelectedMaGrpIdIsNull(doc) && doc.getAmendmentId() != null && ma.getAmendment(doc.getAmendmentId()) != null

&& executedAmendmentTypes.contains(documentType.getId())) {

result.add(documents);

} else if (doc.getSelectedMAGrpId() != null) {

doc.setContainer(ma.getGroup());

if (doc.getSelectedMAList().contains(ma.getId())) {

result.add(documents);

}

} else if (!executedAmendmentTypes.contains(documentType.getId())) {

result.add(documents);

}else {

result.add(documents);

}

}

return result;

}

public static List<List> getNegotiationDocumentExecDocAllGroups(MasterAgreement ma , BeagleRepository repository){

String key = ma.getId().toString();

return docGroupsCache.computeIfAbsent(key,k-> {

List<List> groups = new ArrayList<>();

groups.addAll(ma.getNegotiationDocumentExecDocAllGroups(ma,repository));

return groups;

});

}

private static boolean docIsForGrpAndSelectedMaGrpIdIsNull(NegotiationDocumentForExecDoc doc) {

return doc.isForGroup() && doc.getSelectedMAGrpId() == null;

}

private static boolean docHasSameContainerIdAndSameAmendmentId(NegotiationDocumentForExecDoc doc, MasterAgreement ma) {

return doc.getContainerId().equals(ma.getId()) &&doc.getAmendmentId() != null && ma.getAmendment(doc.getAmendmentId()) != null;

}

/*public static List getExecutedDocumentGroups(MasterAgreement ma, ExecDocTypeFilter documentTypeFilter,boolean isApplicableDoc) {

List result = new ArrayList();

//List docGroups = new ArrayList(ma.getNegotiationDocumentExecDocGroups());

List docGroups = new ArrayList(ma.getAllExecutedDocuments());

Collections.sort(docGroups, new ExecutedDocumentListComparator());

for (Iterator i = docGroups.iterator(); i.hasNext();) {

NegotiationDocumentForExecDoc doc = (NegotiationDocumentForExecDoc) i.next();

NegotiationDocumentType documentType = doc.getType();

if(documentTypeFilter.accept(documentType,ma)){

if(isApplicableDoc){

if(doc.getAmendmentId() != null && ma.getAmendment(doc.getAmendmentId()) != null

&& NegotiationDocumentForExecDoc.EXECUTED_DOCUMENTATION_AMENDMENT_TYPES.contains(documentType.getId()))

{

result.add(doc);

}

else if(!NegotiationDocumentForExecDoc.EXECUTED_DOCUMENTATION_AMENDMENT_TYPES.contains(documentType.getId()))

result.add(doc);

}

else

result.add(doc);

}

}

return result;

}*/

private static void setNegotiationCollateralValidationState(NegotiationDocument document, String collateralDocumentNegoValidationTrigger, Boolean isStandard, Boolean documentClassReset, DocumentParameters p) {

if ((p.documentCatagory != null) && (AddCorrespondence.getColateralDocumentCatagory().contains(p.documentCatagory.getId().toString()))) {

document.setIsStandard(isStandard);

if (documentClassReset != null && documentClassReset) {

document.setCurrentValidationState(DocumentClass.INITIAL_STATE);

}

if (document.getIsStandard() != null) {

if (document.getIsStandard()) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.requestFinalApproval();

}

if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.rejectForNegotiation();

}

if (StandardNegoDocument.TRIGGER_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.requestApprove();

}

} else if (!document.getIsStandard()) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.requestFinalApproval();

}

if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.rejectForNegotiation();

}

if (StandardNegoDocument.TRIGGER_APPROVE.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.requestApprove();

}

if (StandardNegoDocument.TRIGGER_FOR_COMMENT_CONSENT.equalsIgnoreCase(collateralDocumentNegoValidationTrigger)) {

document.requestForCommentConsent();

}

}

}

} else {

document.setIsStandard(null);

document.setCurrentValidationState(null);

}

}

public void sendMail(MasterAgreement ma, Amendment amd, NegotiationDocument latest, String status, List documents, String comments, User uploader) {

String adminLocation;

List<String> toList = new ArrayList<String>();

//String cc ;

List<String> cc = new ArrayList<String>();

String subject = "[ ";

String docUploadEmailAdd = "";

subject += getStatusSubjectForEmail(status);

subject += " -";

String documentationClass;

if (latest != null) {

if (latest.getIsStandard()) {

documentationClass = " Standard";

} else {

documentationClass = " Non Standard";

}

//docUploadEmailAdd = latest.getUploader().getEmail();

docUploadEmailAdd = uploader.getEmail();

cc.add(latest.getNegotiator().getUser().getEmail());

} else {

documentationClass = " Unknown";

}

subject += documentationClass;

subject += " Collateral Documentation ] - ref : ";

if (!ma.belongsToGroup()) {

subject += (ma.getId().toString());

} else {

subject += (ma.getId().toString() + "[group " + ma.getGroup().getId() + " ]");

}

subject += " - cty : ";

if (ma.isStandard()) {

subject += ma.getCounterpartyEntity().getEntityNameLeiAndCrdsForMail();

} else {

if (ma.getCounterpartyIsManaged()) {

subject += ma.getActingEntity(Party.COUNTERPARTY).getEntityNameLeiAndCrdsForMail();

} else {

ma.getActingEntity(Party.BNP_PARIBAS).toString();

}

}

adminLocation = amd.getAdminLocation().getName();

StringBuilder body = new StringBuilder();

body.append("All, \n\n");

body.append("The " + documentationClass + " Collateral Documentation linked to ");

if (ma.belongsToGroup()) {

body.append(" master agreement " + ma.getId().toString() + " - group " + ma.getGroup().getId() + " - " + ma.getAgreementType().toString());

} else {

body.append(" master agreement " + ma.getId().toString() + " - " + ma.getAgreementType().toString());

}

String bodyMailsStatus = getBodyMailsStatus(status);

body.append(" was \"" + bodyMailsStatus + "\" today ");if (SENT_FOR_FINAL_APPROVAL.equals(bodyMailsStatus) || SENT_FOR_COMMENT_CONSENT.equals(bodyMailsStatus)) {

body.append(" from " + " Beagle. \n\n");

} else {

body.append(" in " + " Beagle. \n\n");

}

body.append("Counterparty: " + (ma.isStandard() ? ma.getCounterpartyEntity().getEntityNameLeiAndCrdsForMail() : (ma.getCounterpartyIsManaged() ? ma.getActingEntity(Party.COUNTERPARTY).getEntityNameLeiAndCrdsForMail() : ma.getActingEntity(Party.BNP_PARIBAS).getEntityNameLeiAndCrdsForMail())) + "\n");

if (ma.belongsToGroup()) {

body.append("Collateral Documentation linked to master agreements: \n");

body.append("\nGroup " + ma.getGroup().getId() + "-" + ma.getAgreementType() + " : ");

List<MasterAgreement> masterAgreements = repository.findMasterAgreementsByGroup(ma.getGroup().getId());

int line_count = 0;

for (MasterAgreement masterAgreement : masterAgreements) {

if (masterAgreementShouldBeAdded(masterAgreement)) {

if (line_count == 0) {

body.append(masterAgreement.getId().toString());

body.append(masterAgreement.getId().toString());

body.append(masterAgreement.getCounterpartyEntity().getCrdsCodesWithLEIForMails());

} else {

body.append(", " + masterAgreement.getId().toString());

body.append(masterAgreement.getCounterpartyEntity().getCrdsCodesWithLEIForMails());

}

line_count++;

}

}

} else {

body.append("Collateral Documentation linked to master agreement: ");

body.append(ma.getId().toString() + " - " + ma.getAgreementType().toString());

}

if (SENT_FOR_FINAL_APPROVAL.equals(bodyMailsStatus) || SENT_FOR_COMMENT_CONSENT.equals(bodyMailsStatus) || APPROVED.equalsIgnoreCase(bodyMailsStatus) || REJECTED_FOR_RENEGOTIATION.equalsIgnoreCase(bodyMailsStatus)) {

if (!StringUtils.isEmpty(latest.getSegmentId())) {

body.append("\n");

body.append("Collateral Segment Id : ").append(latest.getSegmentId());

}

}

if (collateralOpsService.isCollateralDocValidatedByOps(ma, latest.getTabId(), latest.getDocumentCatagory())) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equals(status)) {

body.append("\n");if (body.indexOf("Collateral Segment Id :") == -1) {

if (!StringUtils.isEmpty(latest.getSegmentId())) {

body.append("Collateral Segment Id : ").append(latest.getSegmentId());

}

}

body.append("\nNegotiator: " + latest.getNegotiator().getFullName());

body.append("\n\n");

body.append("<u>Direct approval process in Beagle:</u>");

body.append("\n\n");

body.append("To approve/reject the attached Collateral document in Beagle, please click on the following ");

String link = "<a href='" + beagleUrl + ma.getId() + "&collateralSegmentId=" + latest.getSegmentId() + "&amendmentId=" + amd.getId().getAmendmentId() + "&view=#COLLATERAL'>Link</a>";

body.append(link);

body.append("\n");

body.append("Then in the collateral tab, please click on \u00AB modify \u00BB button next to \u00AB Collateral Document Status \u00BB and then choose  \u00AB approve \u00BB or \u00AB reject \u00BB");

body.append("\n");body.append("Please note that if you choose to reject the Collateral Documentation, at least a rejection reason must be chosen in the list, and an optionnal comment can be added as well. ");

}

Location cmoLocation = collateralOpsService.getCmoLocation(ma, Long.parseLong(latest.getSegmentId().split("_")[0]));

if (cmoLocation != null) {

adminLocation = cmoLocation.getName();

} else {

CollateralOpsCmoLocation collateralOpsCmoLocation = repository.findObject(CollateralOpsCmoLocation.class, amd.getAdminLocation().getCode());

adminLocation = collateralOpsCmoLocation != null ? collateralOpsCmoLocation.getCmolocation().getName() : adminLocation;

}

}

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equals(status)) {

if (!collateralOpsService.isCollateralDocValidatedByOps(ma, latest.getTabId(), latest.getDocumentCatagory())) {

body.append("\n\n\n");

body.append("Please approve by replying to this mail. This request doesn't require an approval through " +"Collateral Approved request "+"dashboard.");

}

}body.append("\n\n\n");

body.append("-----------------------------------COMMENTS----------------------------------------\n\n");

body.append(BeagleStringUtils.renderHtml(latest.getEmailNotificationsComments()) + "\n");

if (comments != null) {

body.append(comments + "\n");

}

body.append("-----------------------------------------------------------------------------------\n\n");

body.append("\n\nRegards, \nThe Beagle Team\n\n");

List<BeagleFile> files = new ArrayList<BeagleFile>();

for (Object document : documents) {

NegotiationDocument negotiationDocument = (NegotiationDocument) document;

BeagleFile file = fileSystemAccess.getFile(negotiationDocument.getId().toString(), negotiationDocument.getFileName());

files.add(file);

}

if (isApproveRejectStatus(status)) {          //Invert to and cc list

toList.add(cc.get(0).toString());

toList.add(docUploadEmailAdd);

cc.clear();

cc.add(StringUtils.join(repository.getToMailIdListForAdmLocAndMailTrigger(adminLocation, MAIL_TRIGGER), ","));

} else {

if (latest.getCmoLocation() != null && !latest.getCmoLocation().getName().equals(adminLocation)) {

List<String> locationsList = new ArrayList<String>();

locationsList.add(adminLocation);

locationsList.add(latest.getCmoLocation().getName());

List<String> emailIdsList = repository.getToMailIdListForAdmLocationsAndMailTrigger(locationsList, MAIL_TRIGGER);

toList.addAll(emailIdsList);

} else {

toList = repository.getToMailIdListForAdmLocAndMailTrigger(adminLocation, MAIL_TRIGGER);

}

cc.add(docUploadEmailAdd);

}

List<String> mailGroup = repository.getMailGroupsForAdmLocAndMailTrigger(adminLocation, MAIL_TRIGGER);

mailService.sendCSAMail(toList, cc, subject, body.toString(), files, MAIL_TRIGGER, mailGroup, ma.getId(), adminLocation);

for (Object document : documents) {

NegotiationDocument negotiationDocument = (NegotiationDocument) document;

negotiationDocument.setMailSent(true);

setEmailForSaving(toList, cc, subject, body.toString(), files, negotiationDocument);

}

}

private boolean isApproveRejectStatus(String status) {

String[] APPROVE_REJECT_STATUSES = {DocumentClass.TRIGGER_APPROVE,

DocumentClass.TRIGGER_REJECT_FOR_NEGO,

DocumentClass.TRIGGER_APPROVED,

DocumentClass.STS_REJECTED_BY_CMO};

return BeagleStringUtils.containsAnyString(status, APPROVE_REJECT_STATUSES);

}

protected void setEmailForSaving(List<String> toList, List<String> cc, String subject, String body, List<BeagleFile> files, NegotiationDocument negotiationDocument) {

CSAEmailSaved csaEmailSaved;

if (negotiationDocument.getEmailSent() == null) {

csaEmailSaved = new CSAEmailSaved();

setEmailDetails(toList, cc, subject, body, files, negotiationDocument, csaEmailSaved);

} else {

csaEmailSaved = negotiationDocument.getEmailSent();

setEmailDetails(toList, cc, subject, body, files, negotiationDocument, csaEmailSaved);

}

}

private static void setEmailDetails(List<String> toList, List<String> cc, String subject, String body, List<BeagleFile> files, NegotiationDocument negotiationDocument, CSAEmailSaved csaEmailSaved) {

csaEmailSaved.setSentTime(new Timestamp(new Date().getTime()));

StringBuilder temp = new StringBuilder();

StringBuilder temp2 = new StringBuilder();

int count = 0;

int count2 = 0;

for (String to : toList) {

if (count == 0) {

temp.append(to);

} else {

temp.append(", " + to);

}

count++;

}

csaEmailSaved.setSentTo(temp.toString());

for (String cC : cc) {   // changes added

if (count2 == 0) {

temp2.append(cC);

} else {

temp2.append(", " + cC);

}

count2++;

}

csaEmailSaved.setCc(temp2.toString());

csaEmailSaved.setSubject(subject);

csaEmailSaved.setBody(body);

csaEmailSaved.setReplyTo(temp2.toString());

StringBuilder temp1 = new StringBuilder();

int count1 = 0;

for (BeagleFile file : files) {

if (file.getFileNameAsUploaded() != null) {

if (count1 == 0) {

temp1.append(file.getFileNameAsUploaded());

} else {temp1.append(", " + file.getFileNameAsUploaded());

}

count1++;

}

}

csaEmailSaved.setAttachments(temp1.toString());

csaEmailSaved.setNegotiationDocument(negotiationDocument);

negotiationDocument.setEmailSent(csaEmailSaved);

}

private static boolean masterAgreementShouldBeAdded(MasterAgreement masterAgreement) {

if (masterAgreement.isLiveMA() &&

masterAgreement.hasLiveCsa()) {

return true;

}

return false;

}

private static String getBodyMailsStatus(String status) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equals(status)) {

return SENT_FOR_FINAL_APPROVAL;

}

if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equals(status)) {

return REJECTED_FOR_RENEGOTIATION;

}

if (StandardNegoDocument.TRIGGER_APPROVE.equals(status)) {

return APPROVED;

}

if (StandardNegoDocument.TRIGGER_FOR_COMMENT_CONSENT.equals(status)) {

return SENT_FOR_COMMENT_CONSENT;

}

if (StandardNegoDocument.STS_APPROVED.equals(status)) {

return APPROVED;

}if (StandardNegoDocument.STS_REJECTED_BY_CMO.equals(status)) {

return REJECTED_BY_CMO;

}

return "";

}

private static String getStatusSubjectForEmail(String status) {

if (StandardNegoDocument.TRIGGER_FINAL_APPROVE.equals(status)) {

return APPROVE;

}

if (StandardNegoDocument.TRIGGER_REJECT_FOR_NEGO.equals(status)) {

return REJECTED_FOR_RENEGOTIATION;

}

if (StandardNegoDocument.TRIGGER_APPROVE.equals(status)) {

return APPROVED;

}

if (StandardNegoDocument.TRIGGER_FOR_COMMENT_CONSENT.equals(status)) {

return COMMENT_CONSENT;

}

if (StandardNegoDocument.STS_APPROVED.equals(status)) {

return APPROVED;

}

if (StandardNegoDocument.STS_REJECTED_BY_CMO.equals(status)) {

return REJECTED_BY_CMO;

}

return "";

}

/*private void updateNegoStatusBasedOnValidationStatusForAgreement(NegotiationDocument document, DocumentContainer container, Boolean forAddition, DocumentParameters p) {

CorrespondenceParameters correspondenceParameters = (CorrespondenceParameters) p;

MasterAgreement masterAgreement = MasterAgreement.get(repository, container.getId());

List negotiationDocument_id = repository.getLatestCollateralNegotiationDocumentsForAgreement(masterAgreement.getId().toString());

PendingActionValue collateralpendingActionValue = new PendingActionValue(masterAgreement, LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

if (forAddition) {

if ((DocumentClass.STS_PENDING_FINAL_APPROVAL.equalsIgnoreCase(document.getCurrentValidationState())) && !(correspondenceParameters.negotiationStatusLookupItems.contains(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT))) {

correspondenceParameters.negotiationStatusLookupItems.add(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

} else if  ((correspondenceParameters.negotiationStatusLookupItems.contains(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT)) && ((DocumentClass.STS_REJECTED_FOR_NEGOTIATION.equalsIgnoreCase(document.getCurrentValidationState())) || (DocumentClass.STS_APPROVED.equalsIgnoreCase(document.getCurrentValidationState())))) {

correspondenceParameters.negotiationStatusLookupItems.remove(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

}

} else {

if (negotiationDocument_id.contains(document.getId())) {

if (DocumentClass.STS_PENDING_FINAL_APPROVAL.equalsIgnoreCase(document.getCurrentValidationState())) {

masterAgreement.getPendingActions().add(collateralpendingActionValue);

} else if ((masterAgreement.getPendingActions().contains(collateralpendingActionValue)) && (DocumentClass.STS_REJECTED_FOR_NEGOTIATION.equalsIgnoreCase(document.getCurrentValidationState())) || (DocumentClass.STS_APPROVED.equalsIgnoreCase(document.getCurrentValidationState()))) {masterAgreement.getPendingActions().remove(collateralpendingActionValue);

}

}

}

}

private void updateNegoStatusBasedOnValidationStatusForGroup(NegotiationDocument document, DocumentContainer container, Boolean forAddition, DocumentParameters p) {

CorrespondenceParameters correspondenceParameters = (CorrespondenceParameters) p;

Group group = Group.get(repository, container.getId());

if (forAddition) {

if ((DocumentClass.STS_PENDING_FINAL_APPROVAL.equalsIgnoreCase(document.getCurrentValidationState())) && !(correspondenceParameters.negotiationStatusLookupItems.contains(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT))) {

correspondenceParameters.negotiationStatusLookupItems.add(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

} else if ((correspondenceParameters.negotiationStatusLookupItems.contains(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT)) && ((DocumentClass.STS_REJECTED_FOR_NEGOTIATION.equalsIgnoreCase(document.getCurrentValidationState())) || (DocumentClass.STS_APPROVED.equalsIgnoreCase(document.getCurrentValidationState())))) {

correspondenceParameters.negotiationStatusLookupItems.remove(LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

}

} else {

List negotiationDocument_id = repository.getLatestCollateralNegotiationDocumentsForGroup(group.getId().toString());

List<MasterAgreementLite> masterAgreements = group.getAgreements();

for (int i = 0; i < masterAgreements.size(); i++) {

MasterAgreement masterAgreement = MasterAgreement.get(repository, masterAgreements.get(i).getId());

PendingActionValue pendingActionValue = new PendingActionValue(masterAgreement, LookupItem.PENDING_ACTION_WITH_COLLATERAL_MANAGEMENT);

if (negotiationDocument_id.contains(document.getId())) {

if (DocumentClass.STS_PENDING_FINAL_APPROVAL.equalsIgnoreCase(document.getCurrentValidationState())) {

masterAgreement.getPendingActions().add(pendingActionValue);

} else if ((masterAgreement.getPendingActions().contains(pendingActionValue)) &&  (DocumentClass.STS_REJECTED_FOR_NEGOTIATION.equalsIgnoreCase(document.getCurrentValidationState())) || (DocumentClass.STS_APPROVED.equalsIgnoreCase(document.getCurrentValidationState()))) {

masterAgreement.getPendingActions().remove(pendingActionValue);

}

}

}

}

}*/

public MailService getMailService() {

return mailService;

}

public void setMailService(MailService mailService) {

this.mailService = mailService;

}

public Boolean shouldLoadDataFromPreviousCollateralEntry(DocumentParameters documentParameters, Long container_id) {

if ((getPreviousCollateralEntry(documentParameters, container_id) != null) &&

(documentParameters.documentCatagory != null)) {

if ((getPreviousCollateralEntry(documentParameters, container_id).getDocumentCatagory().getId().equals(documentParameters.documentCatagory.getId())) &&

getPreviousCollateralEntry(documentParameters, container_id).getCurrentValidationState() != null &&

!(getPreviousCollateralEntry(documentParameters, 

 container_id).getCurrentValidationState().equalsIgnoreCase(DocumentClass.STS_APPROVED))) {

getPreviousCollateralEntry(documentParameters,container_id).getCurrentValidationState();

return Boolean.TRUE;

} else {

return Boolean.FALSE;

}

} else {

return Boolean.FALSE;

}

}

protected NegotiationDocument getPreviousCollateralEntry(DocumentParameters documentParameters, Long container_id) {

List collateralEntryIds;

if (documentParameters.isDocumentForGroup) {

collateralEntryIds = repository.getLatestCollateralNegotiationDocumentsForGroup(container_id.toString());

} else {

collateralEntryIds = repository.getLatestCollateralNegotiationDocumentsForAgreement(container_id.toString());

}

if (!collateralEntryIds.isEmpty()) {

Long lastCollateralEntryId = (Long) collateralEntryIds.get(0);

return repository.findSharedOrNonSharedNegotiationDocument(lastCollateralEntryId, documentParameters.containerId, documentParameters.isDocumentForGroup);

} else {

return null;

}

}

public List<Long> clearArchivingTasks(List<Long> removableFileIds) {

List ids = new ArrayList();

for (int i = 0; i < removableFileIds.size(); i++) {

ids.add(removableFileIds.get(i).toString());

}

return repository.clearArchivingTask(ids);

}

}

