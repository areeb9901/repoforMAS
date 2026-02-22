package com.bnpparibas.beagle.ma.services;

import com.bnpparibas.beagle.bulkupload.agreementupload.modal.AgreementUploadData;

import com.bnpparibas.beagle.collaterals.model.CollateralData;

import com.bnpparibas.beagle.collaterals.model.CollateralHeaderDetails; import com.bnpparibas.beagle.collaterals.model.CollateralRegimeDetail;

import com.bnpparibas.beagle.collaterals.model.CollateralStatus;

import com.bnpparibas.beagle.config.helper. LoadLuaKeyTermEnum;

import com.bnpparibas.beagle.config.helper. LoadPbaKeyTermEnum;

import com.bnpparibas.beagle.coveredoffices.model.CoveredOffice;

import com.bnpparibas.beagle.crossdefaults.model.CrossDefault;

import com.bnpparibas.beagle.documents. NegotiationService;

import com.bnpparibas.beagle.documents.model.Attachment;

import com.bnpparibas.beagle.groups.job.model.GroupEditFailedJob;

import com.bnpparibas.beagle.groups.job.model.GroupEditJob;

import com.bnpparibas.beagle.groups.job.model.GroupEditJobStatus;

import com.bnpparibas.beagle.groups.job.model.GroupEditOperationDetail

; import com.bnpparibas.beagle.groups.job.operation.GroupEditJobExecutor;

import com.bnpparibas.beagle.groups.job.service.GroupEditJobNotification;

import com.bnpparibas.beagle.groups.job.service.GroupEditJobService;

import com.bnpparibas.beagle.groups.model.Group;

import com.bnpparibas.beagle.groups.operations.ExecuteGroupOperation;
import com.bnpparibas.beagle.indexing.IndexingService;

import com.bnpparibas.beagle.isdacdealinkagecontrol.services. IsdaCdealinkageControlService;

import com.bnpparibas.beagle.kernel.actions.*;

import com.bnpparibas.beagle.kernel.database. BeagleRepository;

import com.bnpparibas beagle.kernel.logging. BeagleLogger;

import com.bnpparibas.beagle.kernel.operations.*;

import com.bnpparibas.beagle.kernel.security.Auditor;

import com.bmpparibas.beagle.kernel.security.PermissionActions;

import com.bnpparibas.beagle.kernel.security. UserRolePermissions;

import com.bnpparibas.beagle.kernel.services. EntityIds;

import com.bnpparibas.beagle.kernel.services.MailService;

import com.bnpparibas.beagle.kernel.services.MailServiceImpl;

import com.bnpparibas.beagle.kernel.util.*;

import com.bnpparibas.beagle.lei.service.LeiService;

import com.bnpparibas.beagle.luaKeyTerms.model.*;

import com.bnpparibas.beagle.ma.model.*;

import com.bnpparibas.beagle.ma.operations.ExecuteMasterAgreement Operation;

import com.bnpparibas.beagle.ma.operations. OverlapProductInfoOperation;

import com.bnpparibas.beagle.ma.operations. TerminateMasterAgreementOperation;

import com.bnpparibas.beagle.ma.requiredfield. RequiredField Analyser;

import com.bnpparibas.beagle.ma.tableuObject.*;

import com.bnpparibas.beagle.maos.actions.Maos DashBoardActions;

import com.bnpparibas.beagle.maos.model.Maos AuditHistory;

import com.bnpparibas.beagle.pbakeyterms.model.*;

import com.bnpparibas.beagle.pbakeyterms.model.FishOrCutBait;

import com.bnpparibas.beagle.regulationsandprotocols.service. RegulationsAnd ProtocolsService;

import com.bnpparibas.beagle.staticdata.actions. UpdateBeagleOnMaos Feed;

import com.bnpparibas.beagle.staticdata.actions.parameters.CollateralTypeLookup;

import com.bnpparibas.beagle.staticdata.model.*;

import com.bnpparibas.beagle.staticdata.model.Currency;

import com.bnpparibas.beagle.staticdata.model.entity.Entity;

import com.bnpparibas.beagle.tradereconstruction. TradeReconstructionUtils;
import com.thoughtworks.xstream.XStream;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.ArrayUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.exception. ExceptionUtils;

import org.apache.commons.lang.time.DateFormatUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.hssf.util.HSSFColor;

import org.apache.poi.openxml4j.exceptions. InvalidFormatException;

import org.apache.poi.openxml4j.opc.OPCPackage;

import org.apache.poi.openxml4j.opc.PackageAccess;

import org.apache.poi.ss.usermodel.Cell;

import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.*;

import org.hibernate.criterion.DetachedCriteria;

import org.hibernate.criterion. Order;

import org.hibernate.criterion. Restrictions;

import org.springframework.beans.Beans Exception;

import org.springframework.context.ApplicationContext;

import org.springframework.context.ApplicationContextAware;

import org.springframework.context.support. ResourceBundleMessageSource;

import org.springframework.transaction.annotation. Transactional;

import javax.jms.JMSException;

import java.io.*;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.text.SimpleDateFormat;

import java.util.*;

import java.util.stream.Collectors;

import java.io.File;

import java.io.FileOutputStream;

public class BeagleServiceImpl implements BeagleService, ApplicationContextAware {

public static final String NEW_ISDA "New ISDA";

private static final BeagleLogger LOGGER BeagleLogger.getLogger (BeagleServiceImpl.class);

public static final List DOMESTIC_TYPES = Collections.unmodifiableList(ListBuilder.build("FBF", "AFB", "FBFD"));

public static final String DOCUMENTS_UPDATE_ON_ISDA = "Documents update on ISDA";

public static final String ALL = "ALL";

public static final String SSNDA_ISDA_REPORT = "SSNDA ISDA Report";

public static final String MSFTA_REPORT = "MSFTA Weekly Report";

private BeagleRepository repository;

private Collection<Operation> operations;

private NegotiationService negotiationService;

private Auditor auditor;

private ApplicationContext applicationContext;

private TradeReconstructionUtils tradeReconstructionUtils;

private Map<String, String> tabAuditPermissions = new HashMap<String, String>();

private static final String TABS "TABS";

private static final String PRINT_PREVIEW = "PRINT PREVIEW";

private static final String MAIL_MA_IN_NEGO = "MAIL_MA_IN_NEGO"; private static final String MAIL_MA_ENTITY_FINAL = "MAIL_MA_ENTITY_FINAL";

private static final String MAIL_MA_COLLATERAL_IN_NEGO = "MAIL_MA_COLLATERAL_IN_NEGO";


private static final List<String> PERMISSIONS_LDI = Arrays.asList("UnTerminateGroupOperation", "PreExecuteGroupOperation", "TerminateGroupOperation", "DormantGroupOperation", "AddAmendment ToGroupOperation", "AddAgreement ToGroupOperation",

"ExecuteGroupOperation", "UnExecuteGroupOperation",

"ChangeAgreementTypeForGroupOperation",

"ChangeEntities ForGroupOperation", "AddAgreementToGroupUploadOperation");
private static final String TRANSFER_DOCUMENTS = "Transfer";

private static final String EMAIL DOCUMENTS = "Email";

private static final List<String> BNP_ENTITY_LEI = Arrays.asList

("6EWKUOFGVX5QQJHFGT48", "ROMUWSFPUSMPRO8K5P83");

private static final List<String> WM_AGR_TYPES Arrays.asList ("Monaco MA OTC Derivs", "Singapore Hong Kong MA WM", "Swiss MA WM", "TW MA WM");

private static final String BILATERAL "Bilateral";

private IndexingService indexing Service;

private BeagleService beagleService;

private ClientOnboardingService clientOnboardingService;

private IsdaCdeaLinkageControlService isdaCdealinkageControlService;

private RegulationsAndProtocols Service regulationsAndProtocolsService;

private LeiService leiService;

private Resource BundleMessageSource messageSource;

public static final String FILE_FORMAT_XLSX = ".xlsx";

private static final String PBMNA_REPORT = "PBMNA Report";

private static final String PBA_REPORT = "PBA Report";

private static final String SSNDA_REPORT = "SSNDA Report";

private static final String ISDA_REPORT = "ISDA Report";

private static final String PRDR_CEP_REPORT = "CEP Report";
private static final List<String> PBMNA_LIST = Arrays.asList("PB-MNA ID", "Admin Location", "Entity Name", "PB-MNA Netting Flag", "Linked Agreement ID", "Agreement Type", "Netting Flag", "New to Rec");

private static final List<String> SSNDA_ISDA_REPORT_TAB1_COLUMNS = Arrays.asList ("MA ID", "GRP ID", "Status", "Agreement Date", "Execution", "Negotiator", "Negotiator Admin Location", "BNPP Crds Code", "CP Entity CRDS Code", "CP Entity Name", "CP Acting Entity Code", "CP Acting Entity Name"

);

private static final List<String> SSNDA_ISDA_REPORT_TAB2_COLUMNS Arrays.asList ("MA ID", "GRP ID", "Group Document", "Document Category", "Document Type", "Summary", "Document Detail", "Date", "Negotiator", "Negotiator Admin Location", "Uploaded By", "Document Name", "Document id", "Date Of Upload");

private static final List<String> MSFTA REPORT_COLUMNS = Arrays.asList("Group Number", "Agreement Number", "Agreement Type", "Agreement Year",

"Agreement Status", "File Opened Date", "Added to Group Date", "Amendment Date", "Pre Execution /Execution Date", "BNPP Entity Name", "Counterparty Entity Name", "Counterparty CRDS Code");

private static final List<String> PBA_LIST = Arrays.asList ("PBA ID", "Admin Location", "Entity Name", "Netting Flag", "New to Rec");

private static final List<String> SSNDA_LIST = Arrays.asList ("MA Id Document Id", "Document Name", "Document Category", "Document Type", "Negotiator", "Document Date", "Document Upload Date");
private static final List<String> ISDA_LIST = Arrays.asList("ISDA ID", "Admin Location",

"Entity Name", "Netting Flag", "New to Rec");

private GroupEditJobService groupEditJobService;

private AutoExecuteAgreementService autoExecuteAgreement Service;

private OracleDataSource beagleDataSource;

public void setBeagleDataSource (OracleDataSource beagleDataSource) {

this beagleDataSource beagleDataSource;

}

public AutoExecuteAgreementService getAutoExecuteAgreementService() {

return autoExecuteAgreementService;

}

public TradeReconstructionUtils getTradeReconstructionUtils() {

return tradeReconstructionUtils;

}

public void setTradeReconstructionUtils (TradeReconstructionUtils tradeReconstructionUtils) {

this.tradeReconstructionUtils tradeReconstructionUtils;

}

public void setAutoExecuteAgreementService

(AutoExecuteAgreement Service autoExecuteAgreementService) { this.autoExecuteAgreement Service = autoExecuteAgreementService;

}DailySchedularExeStatus dailySchedularExeStatus;

private static final String MA_IN_NEGO_JOB_ID = "MAIL_NEGOTIATOR_FOR_IN_NEGO_AGRMNTS";

private static final String MA_IN_NEGO_JOB_NAME = "schedule.maInNego.notification";

private static final String MA_IN_NEGO_JOB_DESC = "Sending Mail To Negotiator for In Negotiation agreements";

private static final String FILE_SIZE_ZERO_JOB_NAME = "schedule.fileSizeZero.notification";

private static final String FILE_SIZE_ZERO_JOB_ID = "MAIL_NEGOTIATOR_FOR_FILE_SIZE_ZERO_REPORTS";

private static final String FILE_SIZE_ZERO_JOB_DESC = "Sending Mail To user for file size zero reports";

private static final String SEND_MAIL_SSNDA_JOB_NAME = "schedule.ssnda.notification";

private static final String SEND_MAIL_SSNDA_JOB_ID = "MAIL_NEGOTIATOR_FOR_SSNDA_REPORTS";

private static final String SEND_MAIL_SSNDA_JOB_DESC = "Sending Mail To user for SSNDA reports";

private static final String SEND_MAIL_PBMNA_JOB_NAME = "schedule.pbmna.notification";

private static final String SEND_MAIL_PBMNA_JOB_ID = "MAIL_NEGOTIATOR_FOR_PBMNA_REPORTS";
private static final String SEND_MAIL_PBMNA_JOB_DESC = "Sending Mail To user for pbmna reports";

private static final String SSNDA_MAIL_ISDA_JOB_ID="SSNDA_REPORT_FOR_ISDA_BI_MONTHLY";

private static final String SSNDA_MAIL_ISDA_JOB_DESC = "Sending email to users for recently executed ISDA ith SSNDA";

private static final String SSNDA_MAIL_ISDA_JOB_NAME = "schedule.ssndaReport GenerationForIsdaBiMonthly.trigger"; 11 Jira 12364 changes start

private static final String SEND_MAIL_MSFTA_JOB_ID="REPORT_FOR_MSFTA_WEEKLY";

private static final String SEND_MAIL_MSFTA_JOB_DESC = "Sending email to users for recently

created MSFTA, added to group for NY Admin location ";

private static final String SEND_MAIL_MSFTA_JOB_NAME = "schedule.MSFTAWeeklyReport.trigger";

11 jira 12364 changes end

private static final String MA_RTS_JOB_ID = "MAIL_NEGOTIATOR_FOR_RTS_AGRMNTS";

private static final String MA_RTS_JOB_NAME = "schedule.maReadyToSign.notification";

private static final String MA_RTS_JOB_DESC "Sending Mail To Negotiator for Ready To Sign agreements"; private static final String MA_COLLATERAL_IN_NEGO_JOB_ID = "MAIL_NEGOTIATOR_FOR_MA_COLLATERAL_IN_NEGO";

private static final String MA_COLLATERAL_IN_NEGO_JOB_NAME = "schedule.maAndCollateralInNego.notification";

private static final String MA_COLLATERAL_IN_NEGO_JOB_DESC = "Sending mail for Agreement and Collateral In Nego";

private static final String MOVE_GRP_EDIT_FAILED_JOB_ID = "MOVE_GRP_EDIT_FAILED";

private static final String MOVE_GRP_EDIT_FAILED_JOB_NAME = "schedule.groupjob.moveGroupEditFailedJob";

private static final String MOVE_GRP_EDIT_FAILED_JOB_DESC = "Moving Group Edit Failed Job";

private static final String MOVE_GRP_EDIT_COMPLETED_JOB_ID = "MOVE_GRP_EDIT_COMPLETED";

private static final String MOVE_GRP_EDIT_COMPLETED_JOB_NAME = "schedule.groupjob.moveGroupEditCompletedJob";
private static final String MOVE_GRP_EDIT_COMPLETED_JOB_DESC = "Moving Group Edit Completed Job";

private static final String DELETE_GRP_EDIT_OLD_FAILED_JOB_ID = "DELETE_GRP_EDIT_OLD_FAILED";

private static stati final String DELETE_GRP_EDIT_OLD_FAILED_JOB_NAME = "schedule.groupjob.deleteGroupEditFailedJob";

private static final String DELETE_GRP_EDIT_OLD_FAILED_JOB_DESC = "Deleting Group Edit Old Failed Job";

private static final String DELETE_GRP_EDIT_OLD_COMPLETED_JOB_ID = "DELETE_GRP_EDIT_OLD_COMPLETED";

private static final String DELETE_GRP_EDIT_OLD_COMPLETED_JOB_NAME = "schedule.groupjob.deleteGroupEditCompletedJob";

private static final String DELETE_GRP_EDIT_OLD_COMPLETED_JOB_DESC = "Deleting Group Edit Old Completed Job";

private static final String NO_REL_MA_JOB_ID = "NO_REL_MA";

private static final String NO_REL_MA_JOB_NAME = "schedule.norel.notification";

private static final String NO_REL_MA_JOB_DESC = "Mail for No Relation on Master agreements";

private static final String TABLEU_DAILY_XML = "TABLEU_DAILY_XML";

private static final String TABLEU_DAILY_XML_NAME = "schedule.genrateXml.tableu";

private static final String TABLEU_DAILY_XML_DESC = "Daily XML generation for tableu";

private static final String TABLEU_LUA_DAILY_XML = "TABLEU_LUA_DAILY_XML";

private static final String TABLEU_LUA DAILY XML NAME = "schedule.genrateXml.tableu.lua";

private static final String TABLEU_LUA_DAILY_XML_DESC = "Daily XML generation for tableu LUA";

private static final String DO_NOT_TRD_MA_JOB_ID = "DO_NOT_TRADE_MA";

private static final String DO_NOT_TRD_MA_JOB_NAME = "schedule.dntr.notification";

private static final String DO_NOT_TRD_MA_JOB_DESC = "Mail for Do Not Trade on Master agreements";

private static final String SEG_IA_NOTIFICATION_JOB_ID = "SEG_IA_NOTIFICATION";

private static final String SEG_IA_NOTIFICATION_JOB_NAME ="schedule.segia.notification";

private static final String SEG_IA_NOTIFICATION_JOB_DESC = "Seg IA Election Notification automatic changes";

private static final String AUTO_EXECUTE_RTS_MA_JOB_ID = "AUTO_EXECUTE_RTS_MA";

private static final String AUTO_EXECUTE_RTS_MA_JOB_NAME = "schedule.readyToSignAutoExecutor.trigger";

private static final String AUTO_EXECUTE_RTS_MA_JOB_DESC = "Auto execute RTS agreements for final entity";

protected boolean flag;

private String node;

private static final String DETAIL_ERR_MSG = "User is not authorised to use Beagle!";

private static final String FILE_NOT_FOUND = "Exception occured. File not found.";

private static final String LOAD_EXCEPTION = "Exception occured. Unable to load file.";

private static final String FILE_CONVERSION_EXCEPTION = "Exception while trying to convert file from csv to excel.";

private static final String AUTO_UPLOAD_BMR_JOB_ID = "AUTO_UPLOAD_BMR";
private static final String AUTO_UPLOAD_BMR_JOB_NAME = "schedule.autoUploadBMRFile.trigger";

private static final String AUTO_UPLOAD_BMR_JOB_DESC = "Auto upload for ISDA BMR file.";

private static final String AUTO_UPLOAD_RSP_JOB_ID = "AUTO_UPLOAD_RSP";

private static final String AUTO_UPLOAD_RSP_JOB_NAME = "schedule.autoUploadRSPFile.trigger";

private static final String AUTO_UPLOAD_RSP_JOB_DESC = "Auto upload for ISDA RSP file.";

private static final String AUTO_UPLOAD_PRDR_JOB_ID = "AUTO_UPLOAD_PRDR";

private static final String AUTO_UPLOAD_PRDR_JOB_NAME =

"schedule.autoUploadPRDRFile.trigger";

private static final String AUTO_UPLOAD_PRDR_JOB_DESC = "Auto upload for ISDA PRDR file.";

private static final String AUTO_UPLOAD_JMP_JOB_ID = "AUTO_UPLOAD_JMP";

private static final String AUTO_UPLOAD_JMP_JOB_NAME = "schedule.autoUploadJMPFile.trigger";

private static final String AUTO_UPLOAD_JMP_JOB_DESC = "Auto upload for ISDA JMP file.";

private static final String AUTO_UPLOAD_BAIL_IN_JOB_ID = "AUTO_UPLOAD_BAIL_IN";

private static final String AUTO_UPLOAD_BAIL_IN_JOB_NAME = "schedule.autoUploadBAILINFile.trigger";

private static final String AUTO_UPLOAD_BAIL_IN_JOB_DESC = "Auto upload for ISDA BAIL IN file.";
private static final String ISDA_BMR = "ISDA_BMR";

private static final String ISDA_RSP = "ISDA_RSP";

private static final String ISDA_PRDR = "ISDA_PRDR";

private static final String ISDA_JMP = "ISDA_JMP";

private static final String ISDA_BAIL_IN = "ISDA_BAIL_IN";

private static final String FILE_PATTERN = "yyyyMMddHHmm'.xlsx'";

private static final String FORMAT_PATTERN = "yyyyMMddHHmm";

private static final String ISDA_CSV_PATH = "CSV";

private static final String BRRD_CSV_PATH = "CSV";

private static final String BRRDII_RSP = "BRRDII_RSP";

private String filePath;

private String isdaBatchPath;

private String isdaFormURLBatchPath;
private static final String AUTO_UPLOAD_BRRD2_JOB_NAME = "schedule.autoUploadBRRD2File.trigger";

private static final String AUTO_UPLOAD_BRRD2_JOB_ID = "AUTO_UPLOAD_BRRD2";

private static final String AUTO_UPLOAD_BRRD2_JOB_DESC = "Auto upload for ISDA BRRD2 file.";

private String tradeReconstructionInputPath;

private String tradeReconstructionOutputPath;

private String tableuXmlOutputPath;

private String tradeReconstruction ArchivePath;

private String prodUrl;

private String stagingUrl;

private String uatUrl;

public String getIsdaFormURLBatchPath() {

return isdaFormURLBatchPath;

}

public void setIsdaFormURLBatchPath (String isdaFormURLBatchPath) {

this.isdaFormURLBatchPath = isdaFormURLBatchPath;

}

public String getIsdaBatchPath() {

return isdaBatchPath;

}

public void setIsdaBatchPath(String isdaBatchPath) {

this.isdaBatchPath= isdaBatchPath;

}

public String getFilePath() { }

return filePath;

public void setFilePath(String filePath) {

this.filePath = filePath;

}

public String getTradeReconstructionInputPath() {

return tradeReconstructionInputPath;

}

public void setTradeReconstruction InputPath(String trade ReconstructionInputPath) {

this.trade ReconstructionInputPath = trade ReconstructionInputPath;

}

public String getTradeReconstructionOutputPath() { return trade ReconstructionOutputPath;

}public void setTradeReconstructionOutputPath(String tradeReconstructionOutputPath) {

this.tradeReconstructionOutputPath = tradeReconstructionOutputPath;

}

public String getTableuXmlOutputPath() {

return tableuXmlOutputPath;

}

public void setTableuXmlOutputPath(String tableuXmlOutputPath) {

this.tableuXmlOutputPath = tableuXmlOutputPath;

}

public String getTradeReconstructionArchivePath() {

return tradeReconstructionArchivePath;

}

public void setTradeReconstructionArchivePath(String tradeReconstructionArchivePath) {

this.tradeReconstructionArchivePath = tradeReconstructionArchivePath;

}

public String getProdUrl() {

return prodUrl;

}

public void setProdUrl(String prodUrl) {

this.prodUrl = prodUrl;

}

public String getStagingUrl() {

return stagingUrl;

}

public void setStagingUrl(String stagingUrl) {

this.stagingUrl = stagingUrl;

}

public String getUatUrl() {

return uatUrl;

}public void setUatUrl(String uatUrl) {

this.uatUrl = uatUrl;

}

public String getNode() {

return node;

}

public void setNode(String node) {

this.node = node;

}

public void setDailySchedularExeStatus(DailySchedularExeStatus dailySchedularExeStatus) {

this.dailySchedularExeStatus = dailySchedularExeStatus;

}

public void setIsdaCdeaLinkageControlService(IsdaCdeaLinkageControlService isdaCdeaLinkageControlService) {

this.isdaCdeaLinkageControlService = isdaCdeaLinkageControlService;

}

public void setRegulationsAndProtocolsService(RegulationsAndProtocolsService regulationsAndProtocolsService) {

this.regulationsAndProtocolsService = regulationsAndProtocolsService;

}

public BeagleServiceImpl(BeagleRepository beagleRepository, NegotiationService negotiationService, Auditor auditor) {

this.repository = beagleRepository;

this.negotiationService = negotiationService;

this.auditor = auditor;

}

public void setClientOnboardingService(ClientOnboardingService clientOnboardingService) {

this.clientOnboardingService = clientOnboardingService;

}

public void setIndexingService(IndexingService indexingService) {

this.indexingService = indexingService;

}

public IndexingService getIndexingService() {

return indexingService;

}

public GroupEditJobService getGroupEditJobService() {

return groupEditJobService;

}

public void setGroupEditJobService(GroupEditJobService groupEditJobService) {

this.groupEditJobService = groupEditJobService;

}

public ResourceBundleMessageSource getMessageSource() {

return messageSource;

}

public void setMessageSource(ResourceBundleMessageSource messageSource) {

this.messageSource = messageSource;

}

@Override

public List operationsForAgreement(Amendment a) {

int docCategory = a!=null ? a.getMasterAgreement().getAgreementType().getDocCategory().getId() : 0;

List<UserRolePermissions> userPermissions = repository.fetchUserPemissions(auditor.getUser().getRole().getId(),docCategory);

boolean exportDocAdded = false;

List<Operation> result = new ArrayList<Operation>();

for (Iterator iterator = getOperations().iterator(); iterator.hasNext(); ) {

Operation operation = (Operation) iterator.next();

for (Iterator itr = userPermissions.iterator(); itr.hasNext(); ) {

UserRolePermissions userRolePermissions = (UserRolePermissions) itr.next();

for (PermissionActions permissionActions : userRolePermissions.getPermission().getPermissionActionsSet()) {

if (permissionActions.getAction().equalsIgnoreCase(operation.getClass().getSimpleName()) && userRolePermissions.isPermissionReadWrite()) {

if (operation.isAvailable(a)) {

if ("LEGAL_DATA_INTEGRITY".equalsIgnoreCase(auditor.getUser().getRole().getId())) {

if (!PERMISSIONS_LDI.contains(permissionActions.getAction()))

result.add(operation);

} else {

result.add(operation);

}

break;

}

}

if (userRolePermissions.getPermission().getPermission().equalsIgnoreCase(PRINT_PREVIEW) && permissionActions.getAction().equalsIgnoreCase(operation.getClass().getSimpleName()) && userRolePermissions.isPermissionReadOnly()) {

result.add(operation);

}

if (!exportDocAdded && "ExportExecutedDocumentsOperation".equalsIgnoreCase(operation.getClass().getSimpleName()) && ((isUserPermittedToTransferDocumentsAndIsPermissionReadOnly(userRolePermissions)) ||

(isUserPermittedToEmailDocumentsAndIsPermissionReadWrite(userRolePermissions)))) {

exportDocAdded = true;

result.add(operation);

}

}

if (userRolePermissions.getPermissionCategorization().getCategorization().equalsIgnoreCase(TABS)) {

if ("Audit".equalsIgnoreCase(userRolePermissions.getPermission().getPermission())) {

tabAuditPermissions.put(userRolePermissions.getPermissionCategorization().getGroupPermission(), userRolePermissions.getAccessRights());

}

}

}

}

return result;

}private boolean isUserPermittedToTransferDocumentsAndIsPermissionReadOnly(UserRolePermissions userRolePermissions) {

return userRolePermissions.getPermission().getPermission().equalsIgnoreCase(TRANSFER_DOCUMENTS) && userRolePermissions.isPermissionReadWrite();

}

private boolean isUserPermittedToEmailDocumentsAndIsPermissionReadWrite(UserRolePermissions userRolePermissions) {

return userRolePermissions.getPermission().getPermission().equalsIgnoreCase(EMAIL_DOCUMENTS) && userRolePermissions.isPermissionReadWrite();

}

private synchronized Collection<Operation> getOperations() {

if (operations == null) {

operations = applicationContext.getBeansOfType(Operation.class).values();

}

return operations;

}

@Override

public void runOperation(Class operationClass, Parameters p) throws OperationIsNotRunnable {

Operation operation = findOperation(operationClass);

Report authorizationReport = isAuthorised(operation, p);

if (authorizationReport.hasFailures()) {

throw new OperationIsNotRunnable(authorizationReport);

}

Report report = isRunnable(operation, p);

if (report.hasFailures()) {

throw new OperationIsNotRunnable(report);

} else if (operation instanceof TerminateMasterAgreementOperation && repository.getNSDRepositoryCode(p.amendment.getMasterAgreementId()) == null ) {

} else {

report = isWarning(operation, p);

if (ExecuteMasterAgreementOperation.class.equals(operation.getClass())) {

ExecuteMasterAgreementOperation executeMasterAgreementOperation = (ExecuteMasterAgreementOperation) operation;

if (p instanceof ExecuteMasterAgreementOperation.MyParameters) {

executeMasterAgreementOperation.setCollateralData(p.masterAgreement, (ExecuteMasterAgreementOperation.MyParameters) p);

}

}

}

if (report.hasWarnings()) {

throw new OperationHasWarnings(report);

}

GroupEditJobExecutor executor = new GroupEditJobExecutor(repository);

if (executor.isValid(p.masterAgreement, operationClass.getName()) && !p.multipleTerminate) {

Group group = p.masterAgreement.getGroup();

if (executor.isAlreadyRequested(group.getId())) {

((GroupReplicationParameters) p).setGroupEditJobSubmitted(true);

} else if (executor.canRunAsBackgroundJob(p, group.getAgreements().size())) {

GroupEditOperationDetail operationDetail = executor.getOperationDetail();

GroupEditJob job = new GroupEditJob(group.getId(), operationDetail, p, auditor.getUser(), GroupEditJobStatus.NOT_STARTED);

repository.save(job);

LOGGER.info("Created " + operationClass.getSimpleName() + " job for Group " + group.getId() + ": " + job.getId());

} else {

operation.execute(p);

}

}

else {

operation.execute(p);

}

}

@Override

public Report isRunnable(Class operationClass, Amendment amendment) {

Operation operation = findOperation(operationClass);

Parameters p = new Parameters();

p.amendment = amendment;

return isRunnable(operation, p);

}

private Report isRunnable(Operation operation, Parameters p) {

Report report = new Report(operation, p);

operation.isRunnable(report);

return report;

}private Report isAuthorised(Operation operation, Parameters p) {

Report report = new Report(operation, p);

operation.isAuthorised(report, p);

return report;

}

private Report isWarning(Operation operation, Parameters p) {

Report report = new Report(operation, p);

operation.isWarning(report);

return report;

}

private Operation findOperation(Class operationClass) {

if (getOperations() == null) {

throw new BeagleError(BeagleServiceImpl.class, "Operations not set on " + this + " (maybe you should go to previous page and try again)");

}

for (Iterator iterator = getOperations().iterator(); iterator.hasNext(); ) {

Operation operation = (Operation) iterator.next();

if (operation.getClass() == operationClass) {

return operation;

}

}

throw new BeagleRuntimeException(BeagleServiceImpl.class, "Could not find operation " + operationClass + " - check operations.xml file");

}

@Override

public MasterAgreement previewMasterAgreement(AgreementType agreementType,

EntityIds counterparty, EntityIds bnpParibas,

String negotiator,

Location adminLocation, Long governingLaw, String domesticBranch,

Date fileOpenedDate,

String agreementWithCSA,

boolean createDefaultOffices, Long legalCounterpartyType, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, boolean doSave, Long maosRequestId, CollateralTypeLookup collateralType, Date collNegoStartDate,

LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract, LookupItem partyPostingCollateral,LookupItem acceptanceType

) throws InvalidMasterAgreementException {

MasterAgreement masterAgreement = new MasterAgreement();

masterAgreement.setAgreementType(agreementType);

setCounterpartyEntities(masterAgreement, counterparty);

setBnpParibasEntities(masterAgreement, bnpParibas);

masterAgreement.setFileOpenedDate(fileOpenedDate);

masterAgreement.setWithCSA(AmendmentType.hasCSA(agreementWithCSA));

Long entityId = bnpParibas.entity;Entity bnpEntity = Entity.get(repository, entityId);

boolean containsFortisEntity = bnpEntity.getLegalEntityIdentifier() !=null ?

ArrayUtils.contains(Entity.getBnppFortisEntitiesLEI(), bnpEntity.getLegalEntityIdentifier().getLeiId()) : false;

if (!doSave) {

// boolean contains = ArrayUtils.contains(Entity.getBnppFortisEntityIds(), bnpEntity.getId()) || ArrayUtils.contains(Entity.getBnppFortisEntities(), bnpEntity.getRmpmGroupCode());

masterAgreement.setFortisFlag(Boolean.valueOf(containsFortisEntity));

} else {

masterAgreement.setFortisFlag(containsFortisEntity ? (fortisFlag == null ? Boolean.FALSE : fortisFlag) : Boolean.FALSE);

}

createAndAddFirstAmendment(masterAgreement, adminLocation, negotiator, governingLaw, domesticBranch, agreementWithCSA,legalCounterpartyType, doSave, closeOutNettingFlag, transactionSpecificMasters, collateralType, collNegoStartDate, regulatoryClassification, collateralMarginType, collateralRegime, StandardisedContract, partyPostingCollateral);

if (createDefaultOffices) {

AgreementHelper.createDefaultOffices(masterAgreement, repository);

}

if (maosRequestId != null) {

MaosDetails maosDetails = repository.getObject(MaosDetails.class, maosRequestId);

if (maosDetails != null) {

MaosIdJoin maosIdJoin = new MaosIdJoin(masterAgreement, maosDetails);

masterAgreement.setLinkedMaosJoin(maosIdJoin);

maosDetails.getMaosLinkedMAs().add(maosIdJoin);

}

}

masterAgreement.validateEntities();

masterAgreement.setInitialDefaults();

if(acceptanceType!=null){

AcceptanceTypeEsa acceptanceTypeEsa = new AcceptanceTypeEsa(acceptanceType);

masterAgreement.setAcceptanceTypeEsa(acceptanceTypeEsa);

acceptanceTypeEsa.setMasterAgreement(masterAgreement);

}

masterAgreement.setGuaranteeFlagBnp(false);

masterAgreement.setGuaranteeFlagCp(false);NettingCalculator calculator = new NettingCalculator(masterAgreement, LegalOpinion.getLegalOpinion(repository, masterAgreement));

calculator.calculateCoveredProductNetting(true);

//NO MASTER AGREEMENT PRDR EMIR CREATION

if(null!= masterAgreement.getAgreementType() && masterAgreement.getAgreementType().isNoMasterPRDRAgreement()) {

Set<EMIRSpecialClauseJoin> targetEmirSpecialClauseJoins = new HashSet<>();

targetEmirSpecialClauseJoins.add(copyEMIRPRDRClauseForNMPRDRAgr(masterAgreement,null));

masterAgreement.setEmirSpecialClauseJoins(targetEmirSpecialClauseJoins);

}

autoLinkExecutionAgreements(masterAgreement);

return masterAgreement;

}

@Override

public MasterAgreement createAndSaveMasterAgreement(AgreementType agreementType, EntityIds counterparty, EntityIds bnpParibas,

String negotiator, Location adminLocation, Long governingLaw,

String domesticBranch, Date fileOpenedDate, String agreementWithCSA,boolean createDefaultOffices, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, Long legalCounterpartyType, Long maosRequestId, List clearingHouses, CollateralTypeLookup collateralType, Date collNegoStartDate, User user,

LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract, LookupItem partyPostingCollateral,

Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate, Boolean addIsdaCommonFields, LookupItem acceptanceType, Boolean isAutoCreateCdea, boolean shouldSetDerivDataForBulkUpload, AgreementUploadData agreementUploadData) throws InvalidMasterAgreementException {

MasterAgreement masterAgreement = previewMasterAgreement(agreementType, counterparty, bnpParibas, negotiator,

adminLocation, governingLaw, domesticBranch, fileOpenedDate, agreementWithCSA, createDefaultOffices,legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters, true, maosRequestId, collateralType, collNegoStartDate,

regulatoryClassification, collateralMarginType, collateralRegime, StandardisedContract, partyPostingCollateral,acceptanceType);

String agreementTypeId = masterAgreement.getAgreementType().getId().getId();

String action = "";

if (isClearingHouseAgreementType(agreementTypeId)) {

setClearingHouses(clearingHouses, masterAgreement);

}

if (AgreementType.OUTRIGHT_BNL_AGREEMENT.equalsIgnoreCase(agreementTypeId)) {

setLegalData(masterAgreement);

setCrossDefaultData(masterAgreement);

}

if (AgreementType.getReportingServiceAgreements().contains(agreementTypeId)) {

setCrossDefaultData(masterAgreement);

setLegalDataForReportingServiceAgreement(masterAgreement);

}

if (isNoMasterOrFbfdAgreementType(masterAgreement, agreementTypeId) && !AgreementHelper.isFbfEligibleForPrefilling(masterAgreement)) {

EMIRData emirData = new EMIRData(masterAgreement);emirData.setDisputeReporting(Boolean.TRUE);

emirData.setTradeReporting(Boolean.TRUE);

masterAgreement.setEMIRData(emirData);

}

if (AgreementType.FBFD.equals(agreementTypeId) && ("2013".equals(masterAgreement.getAgreementType().getYear())) && !AgreementHelper.isFbfEligibleForPrefilling(masterAgreement)) {

masterAgreement.setCounterpartyCreditEventUponMerger(Boolean.TRUE);

masterAgreement.setBnpParibasCreditEventUponMerger(Boolean.TRUE);

masterAgreement.setCounterpartyAutomaticTermination(Boolean.FALSE);

masterAgreement.setBnpParibasAutomaticTermination(Boolean.FALSE);

masterAgreement.setTerminationCurrencyOfTrade("Currency Of Trade");

}

if(AgreementType.EMAD.equals(agreementTypeId) && StandardisedContract != null && StandardisedContract == true && adminLocation.isBrussel() &&

masterAgreement.getBnpLei().equals("KGCEPHLVVKVRZYO1T647") && (masterAgreement.isStandard() || masterAgreement.isSingleManaged())){

AgreementHelper.setDataForEMAD(masterAgreement, repository);

}

if(masterAgreement.isSyntheticRepo() && !masterAgreement.isPrivate()){

AgreementHelper.setDataForSyntheticRepo(masterAgreement,repository);

}

masterAgreement.setNegoStatusCommentLastModifiedDate(new Date());

masterAgreement.updateNegotiationDateForMa();

if (addIsdaCommonFields) {

saveDefaultValuesCrossDefaultTab(masterAgreement);

saveDefaultValuesLegalTab(masterAgreement);

saveDefaultBnpOfficeCovered(masterAgreement);

}else if(masterAgreement.isAgreementTypeIsda() && "2002".equals(masterAgreement.getAgreementType().getYear())){

final List<AdditionalTerminationEvent> terminationEvents = repository.findAdditionalTerminationEvents();

terminationEvents.stream().forEach(event -> {if( (TerminationEvent.BNP_FORCE_MAJEURE.getId().equals(event.getId()) ||

TerminationEvent.CP_FORCE_MAJEURE.getId().equals(event.getId()))) {

masterAgreement.addAdditionalTerminationEvent(new TerminationEvent(masterAgreement, (Long) event.getId(), "Y"));

}

});

}if(isAutoCreateCdea)

isdaCdeaLinkageControlService.removeFieldsForCdea(masterAgreement);

if(shouldSetDerivDataForBulkUpload){

setExecutionDataForBulkUpload(masterAgreement,agreementUploadData);

}

if(((masterAgreement.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(masterAgreement)) ||

masterAgreement.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(masterAgreement.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(masterAgreement.getGoverningLaw()).toString()) == 6 : false) {

masterAgreement.setCounterpartyAutomaticTermination(true);

masterAgreement.setBnpParibasAutomaticTermination(true);

}

repository.save(masterAgreement);

repository.getSession().flush();

createAndSaveCreditQuestionnaireDetailsForNegotiation(masterAgreement, creditQuestionnaireReceived, creditQuestionnaireReceivedDate);

LOGGER.info("Master Agreement Created with Id : " + masterAgreement.getId());updateContactAndCorrespondence(masterAgreement);

action = "CreateMasterAgreement";

if(!AgreementCategoryHelper.isCategoryRestricted(masterAgreement.getAgreementType().getCategory())){

Entity entity = repository.findObject(Entity.class, counterparty.entity);

regulationsAndProtocolsService.assignIsdaRspAndIsdaJmpProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(), repository);

regulationsAndProtocolsService.assignArt55BailInProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(), repository);

regulationsAndProtocolsService.updateSFTRDateForMA(masterAgreement);

}

if(maosRequestId!=null){

saveMaosAuditData(maosRequestId,masterAgreement, user, repository, action);

sendNotificationToMaos(masterAgreement.getAllLinkedMaosDetails(), maosRequestId);

}if(addIsdaCommonFields) {

repository.updateAuditCommentIsdaCommonFields(masterAgreement.getId());

}

if(agreementType.isESA()){

AgreementHelper.setDataForCountryAnnexWithAgreement(masterAgreement,repository);

}

setSftrFlagForCreateMa(masterAgreement);

return masterAgreement;

}

private boolean isNoMasterOrFbfdAgreementType(MasterAgreement masterAgreement, String agreementTypeId) {

return AgreementType.NO_MASTER_REPORTING_SERVICE_AGMT.contains(agreementTypeId) || (AgreementType.FBFD.equals(agreementTypeId) && ("2013".equals(masterAgreement.getAgreementType().getYear()) || "2007".equals(masterAgreement.getAgreementType().getYear())));

}

public void sendNotificationToMaos(List<MaosDetails> maosDetails, Long maosId) {

for(MaosDetails maosDetail : maosDetails) {

if ( maosDetails != null && BeagleNumberUtils.isLongEquals(maosId, maosDetail.getMaosId())  ) {

try {

clientOnboardingService.sendNotificationToMaos(maosDetail, MaosNotification.get(maosDetail.getMaosIdStatus()));} catch (JMSException e) {

LOGGER.error("Error while sending notification to MAOS", e);

}

}

}

}

@Override

public void createAndSaveCreditQuestionnaireDetailsForNegotiation(MasterAgreement masterAgreement, Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate) {

setCreditQuestionnaireDetailsForNegotiation(masterAgreement, creditQuestionnaireReceived, creditQuestionnaireReceivedDate);

AgreementHelper.setDefaultNegotiationStatus(masterAgreement, creditQuestionnaireReceived, repository);

}

private void setCreditQuestionnaireDetailsForNegotiation(MasterAgreement masterAgreement, Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate) {

if( creditQuestionnaireReceived != null ) {

Negotiation negotiation = repository.findOrCreateNegotiationForAgreement(masterAgreement);

negotiation.setCreditQuestionnaireReceived(creditQuestionnaireReceived);

negotiation.setCreditQuestionaireReceivedDate(creditQuestionnaireReceivedDate);

}

}private void saveMaosAuditData(Long maosRequestId, MasterAgreement masterAgreement, User user, BeagleRepository repository, String action) {

Long maId = masterAgreement.getMa().getId();

Long groupId = masterAgreement.getMa().getGrp_id();

String maGroupId = maId.toString();

if(groupId!=null){

maGroupId = maGroupId+"/"+groupId.toString();

}

MaosAuditHistory auditHistory = new MaosAuditHistory(maosRequestId,null,null,maGroupId,null,

MaosDashBoardActions.CREATE_A_NEW_MA,new Date(),user.getFullname());

repository.save(auditHistory);

}

private void setLegalData(MasterAgreement masterAgreement) {

masterAgreement.setCounterpartyCreditEventUponMerger(Boolean.TRUE);

masterAgreement.setBnpParibasCreditEventUponMerger(Boolean.FALSE);

masterAgreement.setCounterpartyAutomaticTermination(Boolean.TRUE);

masterAgreement.setBnpParibasAutomaticTermination(Boolean.TRUE);

masterAgreement.setFullTwoWayPayment(Boolean.TRUE);

//masterAgreement.setCapacityAndAuthority(Boolean.TRUE);

}private void setLegalDataForReportingServiceAgreement(MasterAgreement masterAgreement) {

masterAgreement.setCounterpartyCreditEventUponMerger(Boolean.FALSE);

masterAgreement.setBnpParibasCreditEventUponMerger(Boolean.FALSE);

masterAgreement.setCounterpartyAutomaticTermination(Boolean.FALSE);

masterAgreement.setBnpParibasAutomaticTermination(Boolean.FALSE);

masterAgreement.setSchedule("N");

masterAgreement.setFullTwoWayPayment(Boolean.FALSE);

//masterAgreement.setCapacityAndAuthority(Boolean.FALSE);

}

private void setCrossDefaultData(MasterAgreement masterAgreement) {

masterAgreement.getOrCreateBnpParibasCrossDefault().setType(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION);

masterAgreement.getOrCreateCounterpartyCrossDefault().setType(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION);

}

@Override

public MasterAgreement createAndSaveGroup(AgreementType agreementType, EntityIds counterparty, EntityIds bnpParibas,

List entityIds, String negotiator, Location adminLocation,

Long governingLaw, String domesticBranch, Date fileOpenedDate,

String agreementWithCSA, String ackComment, boolean acknowledge, List<Long> overlappedIds, boolean createDefaultOffices, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, Long legalCounterpartyType,

List<PartyMaosDetails> maosRequest, List clearingHouses, User user, Boolean StandardisedContract, Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate, Boolean addIsdaCommonFields, LookupItem clientRegCategoryBucket, Boolean euroOperation, List<MasterAgreement> maList, Date agreementExecutionDate, Boolean shouldExecute, boolean shouldSetDerivDataForExecution, AgreementUploadData agreementUploadData) throws InvalidMasterAgreementException {

MasterAgreement result = null;

Group group = new Group();

repository.save(group);

LOGGER.info("Group Created with Group Id : " + group.getId());

boolean managedByCounterparty = counterparty.actingEntity != null;

String action = "";

for (Iterator iterator = entityIds.iterator(); iterator.hasNext(); ) {

Long entityId = (Long) iterator.next();

if (managedByCounterparty) {

counterparty.entity = entityId;

} else {

bnpParibas.entity = entityId;

}

PartyMaosDetails maos = PartyMaosDetails.getPartyMaosDetailsByParty(maosRequest, counterparty.entity);

MasterAgreement masterAgreement = previewMasterAgreement(agreementType, counterparty, bnpParibas,

negotiator, adminLocation, governingLaw, domesticBranch, fileOpenedDate, agreementWithCSA,

createDefaultOffices, legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters, true, maos != null ?

maos.getMaosId() : null, null, null, null, null, null, StandardisedContract, null,null);masterAgreement.setGroup(group);

masterAgreement.setAddToGroupDate(new Date());

String agreementTypeId = masterAgreement.getAgreementType().getId().getId();

if (isClearingHouseAgreementType(agreementTypeId)) {

setClearingHouses(clearingHouses, masterAgreement);

}

if (AgreementType.OUTRIGHT_BNL_AGREEMENT.equalsIgnoreCase(agreementTypeId)) {

setLegalData(masterAgreement);

setCrossDefaultData(masterAgreement);

}

if (AgreementType.getReportingServiceAgreements().contains(agreementTypeId)) {

setCrossDefaultData(masterAgreement);

setLegalDataForReportingServiceAgreement(masterAgreement);

}

if (isNoMasterOrFbfdAgreementType(masterAgreement, agreementTypeId)) {

EMIRData emirData = new EMIRData(masterAgreement);

emirData.setDisputeReporting(Boolean.TRUE);

emirData.setTradeReporting(Boolean.TRUE);

masterAgreement.setEMIRData(emirData);

}

if (addIsdaCommonFields) {

saveDefaultValuesCrossDefaultTab(masterAgreement);

saveDefaultValuesLegalTab(masterAgreement);

saveDefaultBnpOfficeCovered(masterAgreement);

}else if(masterAgreement.isAgreementTypeIsda() && "2002".equals(masterAgreement.getAgreementType().getYear())){

final List<AdditionalTerminationEvent> terminationEvents = repository.findAdditionalTerminationEvents();

terminationEvents.stream().forEach(event -> {if( (TerminationEvent.BNP_FORCE_MAJEURE.getId().equals(event.getId()) ||

TerminationEvent.CP_FORCE_MAJEURE.getId().equals(event.getId()))) {

masterAgreement.addAdditionalTerminationEvent(new TerminationEvent(masterAgreement, (Long) event.getId(), "Y"));

}

});

}

if(AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory())){

masterAgreement.setExecutionDate(agreementExecutionDate);

if(overlappedIds != null && overlappedIds.contains(entityId)) {

masterAgreement.setOverlappedProdComm(ackComment);

masterAgreement.setAcknowledgement(acknowledge);

}else if(!managedByCounterparty){

masterAgreement.setOverlappedProdComm(ackComment);

masterAgreement.setAcknowledgement(acknowledge);

}masterAgreement.updateRelationDate();

}

if(shouldExecute || (masterAgreement.isSyntheticRepo() && !masterAgreement.isPrivate())){

masterAgreement.setExecutionDate(agreementExecutionDate);

Amendment amendment = masterAgreement.getAmendments().get(0);

if(masterAgreement.isSyntheticRepo()){

amendment.execute(user,new Date());

} else if(agreementUploadData!=null && agreementUploadData.getAgreementExecDate()!=null)

amendment.execute(user,agreementUploadData.getAgreementExecDate());

else

amendment.execute(user,agreementUploadData.getAgreementSignDate());

masterAgreement.updateRelationDate();

}

if(masterAgreement.isSyntheticRepo() && !masterAgreement.isPrivate()){

AgreementHelper.setDataForSyntheticRepo(masterAgreement,repository);

}

if(shouldSetDerivDataForExecution){

setExecutionDataForBulkUpload(masterAgreement,agreementUploadData);

}

if(AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory()))

AgreementHelper.updatePbLegalData(masterAgreement,clientRegCategoryBucket,euroOperation);

if(maList!=null)

maList.add(masterAgreement);

if(((masterAgreement.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(masterAgreement)) ||

masterAgreement.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(masterAgreement.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(masterAgreement.getGoverningLaw()).toString()) == 6 : false) {

masterAgreement.setCounterpartyAutomaticTermination(true);

masterAgreement.setBnpParibasAutomaticTermination(true);

}

repository.save(masterAgreement);

repository.getSession().flush();

LOGGER.info("Master Agreement Created with Id : " + masterAgreement.getId() + " within Group : " + group.getId());

action = "Create Group Agreement";

masterAgreement.setGrp_id(group.getId());updateContactAndCorrespondence(masterAgreement);

createAndSaveCreditQuestionnaireDetailsForNegotiation(masterAgreement, creditQuestionnaireReceived, creditQuestionnaireReceivedDate);

masterAgreement.updateNegotiationDateForMa();

if(maos!=null){

saveMaosAuditData(maos.getMaosId(),masterAgreement, user, repository, action);

sendNotificationToMaos(masterAgreement.getAllLinkedMaosDetails(), maos.getMaosId());

}

if (result == null) {

result = masterAgreement;

}

isdaCdeaLinkageControlService.checkAndCreateMasterAgreement(masterAgreement, domesticBranch, createDefaultOffices, StandardisedContract, counterparty, bnpParibas);

if(!AgreementCategoryHelper.isCategoryRestricted(masterAgreement.getAgreementType().getCategory())){

Entity entity = repository.findObject(Entity.class, counterparty.entity);

regulationsAndProtocolsService.assignIsdaRspAndIsdaJmpProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(),repository);

regulationsAndProtocolsService.assignArt55BailInProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(),repository);

regulationsAndProtocolsService.updateSFTRDateForMA(masterAgreement);

}

if(addIsdaCommonFields) {

repository.updateAuditCommentIsdaCommonFields(masterAgreement.getId());

}

if(AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory())){

Amendment amendment = repository.findMaster(masterAgreement.getId());

amendment.execute(user, masterAgreement.getExecutionDate());

}

setSftrFlagForCreateMa(masterAgreement);

}

return result;

}

@Override

public MasterAgreement createAndSaveMultiMultiGroup(AgreementType agreementType, EntityIds counterparty, EntityIds bnpParibas,

List cpEntities,List bnpEntities, String negotiator, Location adminLocation,

Long governingLaw, String domesticBranch, Date fileOpenedDate,

String agreementWithCSA, String ackComment, boolean acknowledge, List<Long> overlappedIds, boolean createDefaultOffices, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, Long legalCounterpartyType,

List<PartyMaosDetails> maosRequest, List clearingHouses, User user, Boolean StandardisedContract, Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate, Boolean addIsdaCommonFields, LookupItem clientRegCategoryBucket, Boolean euroOperation, List<MasterAgreement> maList, Date agreementExecutionDate) throws InvalidMasterAgreementException {

MasterAgreement result = null;

Group group = new Group();

repository.save(group);

LOGGER.info("Group Created with Group Id : " + group.getId());

String action = "";

for (Object cp : cpEntities  ) {

for (Object bnp : bnpEntities){

counterparty.entity = (Long) cp;

bnpParibas.entity = (Long) bnp;

PartyMaosDetails maos = PartyMaosDetails.getPartyMaosDetailsByParty(maosRequest, counterparty.entity);MasterAgreement masterAgreement = previewMasterAgreement(agreementType, counterparty, bnpParibas,

negotiator, adminLocation, governingLaw, domesticBranch, fileOpenedDate, agreementWithCSA,

createDefaultOffices, legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters, true, maos != null ?

maos.getMaosId() : null, null, null, null, null, null, StandardisedContract, null,null);

masterAgreement.setGroup(group);

masterAgreement.setAddToGroupDate(new Date());

masterAgreement.setMultiMultiGroup(true);

if(AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory())){

masterAgreement.setExecutionDate(agreementExecutionDate);

}

AgreementHelper.updatePbLegalData(masterAgreement,clientRegCategoryBucket,euroOperation);

if(maList!=null)

maList.add(masterAgreement);

masterAgreement.updateRelationDate();

if(((masterAgreement.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(masterAgreement)) ||

masterAgreement.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(masterAgreement.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(masterAgreement.getGoverningLaw()).toString()) == 6 : false) {

masterAgreement.setCounterpartyAutomaticTermination(true);

masterAgreement.setBnpParibasAutomaticTermination(true);

}

repository.save(masterAgreement);

repository.getSession().flush();

LOGGER.info("Master Agreement Created with Id : " + masterAgreement.getId() + " within Group : " + group.getId());

action = "Create Group Agreement";

masterAgreement.setGrp_id(group.getId());

updateContactAndCorrespondence(masterAgreement);

createAndSaveCreditQuestionnaireDetailsForNegotiation(masterAgreement, creditQuestionnaireReceived, creditQuestionnaireReceivedDate);

masterAgreement.updateNegotiationDateForMa();

if(maos!=null){

saveMaosAuditData(maos.getMaosId(),masterAgreement, user, repository, action);

sendNotificationToMaos(masterAgreement.getAllLinkedMaosDetails(), maos.getMaosId());

}

if (result == null) {

result = masterAgreement;

}

if(AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory())){

Amendment amendment = repository.findMaster(masterAgreement.getId());

amendment.execute(user, masterAgreement.getExecutionDate());

}

setSftrFlagForCreateMa(masterAgreement);

}

}

return result;

}

private void saveDefaultValuesCrossDefaultTab(MasterAgreement ma) {

saveCpCrossDefaultValues(ma);

saveBnpCrossDefaultValues(ma);

ma.setSpecifiedIndebtednessValues(true, LookupItem.getSpecIndebtListDepositCarvedOut());

}

private void saveCpCrossDefaultValues(MasterAgreement ma) {

CrossDefault crossDefault = ma.getOrCreateCounterpartyCrossDefault();

crossDefault.setAllParameters(CrossDefaultType.PERCENTAGE, null, null

, "", false, null, false, LookupItem.EQUITY_TYPE_NAV, 3.0F, null,
false, false, null, false, AffiliateType.AFFILIATE_TYPE_ANY,

null, null, "");

}

private void saveBnpCrossDefaultValues(MasterAgreement ma) {

CrossDefault crossDefault = ma.getOrCreateBnpParibasCrossDefault();

crossDefault.setAllParameters(CrossDefaultType.PERCENTAGE, null, null

, "", false, null, false, LookupItem.EQUTY_TYPE_SHE, 3.0F, null,

false, false, null, false, AffiliateType.AFFILIATE_TYPE_ANY,

null, null, "");

}

private void saveDefaultValuesLegalTab(MasterAgreement ma) {

ma.setTerminationCurrency(Currency.find(repository,"USD"));

ma.setSchedule("N");

ma.setCounterpartyCreditEventUponMerger(true);

ma.setBnpParibasCreditEventUponMerger(true);

ma.setCounterpartyAutomaticTermination(false);

ma.setBnpParibasAutomaticTermination(false);

ma.setAutomaticTermination(Boolean.FALSE);

AutomaticEarlyTermination bnpDetails=new AutomaticEarlyTermination(new AgreementKey(ma, Party.BNP_PARIBAS.getAbbreviatedId()));

bnpDetails.setCanActivated(false);

bnpDetails.setActivationReason(null);

AutomaticEarlyTermination cpDetails=new AutomaticEarlyTermination(new AgreementKey(ma, Party.COUNTERPARTY.getAbbreviatedId()));

cpDetails.setCanActivated(false);

cpDetails.setActivationReason(null);

saveDefaultSpecialClauses(ma);

saveDefaultAddTermEvents(ma);

}

private void saveDefaultSpecialClauses(MasterAgreement ma){

final List<LookupItem> specialClauses = repository.findSpecialClauses();

specialClauses.stream().forEach(clause -> {

if (clause.getId().equals(LookupItem.SPC_NEW_CTD_CODE) && !isMAEligileForDefaultCTD(ma)) {

ma.addSpecialClause(AgreementHelper.getSpecialClauseInstance(ma, (Long) clause.getId(), true));

}else if (!clause.getId().equals(LookupItem.SPC_NEW_CTD_CODE)){

ma.addSpecialClause(AgreementHelper.getSpecialClauseInstance(ma, (Long) clause.getId(), false));

}

});

ma.setHasSpecialClauses(true);

}private void saveDefaultAddTermEvents(MasterAgreement ma){

final List<AdditionalTerminationEvent> terminationEvents = repository.findAdditionalTerminationEvents();

terminationEvents.stream().forEach(event -> {if(isEventEligble(event) ){

ma.addAdditionalTerminationEvent(new TerminationEvent(ma, (Long) event.getId(), "Y"));

}else {

ma.addAdditionalTerminationEvent(new TerminationEvent(ma, (Long) event.getId(), "N"));

}

});

ma.setHasAdditionalTerminationEvents(true);

}

private boolean isEventEligble(AdditionalTerminationEvent event){

return event.getId().toString().equals("107") || event.getId().toString().equals("45") || event.getId().toString().equals("43")

|| event.getId().toString().equals("3") || event.getId().toString().equals("4") || event.getId().toString().equals("42")

|| (TerminationEvent.BNP_FORCE_MAJEURE.getId().equals(event.getId())) ||

TerminationEvent.CP_FORCE_MAJEURE.getId().equals(event.getId());

}

private void saveDefaultBnpOfficeCovered(MasterAgreement ma){

List existing= ma.getBnpParibasCoveredOffices().stream()

.map(coveredOffice -> coveredOffice.getId().getJoinedId()).collect(Collectors.toList());

Arrays.stream(Entity.getBnppIsdaCommonTermsEntities()).filter(id-> !existing.contains(id)).forEach(ids -> ma.addBnpParibasCoveredOffice(createCoveredOffice(ma,Entity.find(repository,ids))));

}

public static CoveredOffice createCoveredOffice(MasterAgreement masterAgreement, Entity entity) {

CoveredOffice office = new CoveredOffice(masterAgreement, entity);

office.setCloseOutNetting(false);

office.setRingFenced(false);

office.setCeasedTrading(false);

return office;

}

@SuppressWarnings("squid:S1067")

private boolean isClearingHouseAgreementType(String agreementTypeId) {

return AgreementType.EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.EUROPEAN_EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.CUSTOMER_ACCOUNT_AGREEMENT.equalsIgnoreCase(agreementTypeId)|| AgreementType.CDEA_EUROPEAN_EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.DRV_GERMAN_EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.FBF_FRENCH_EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.EUROMASTER_EXECUTION_AGREEMENT.equalsIgnoreCase(agreementTypeId)

|| AgreementType.BP2S_RULE_BOOK.equalsIgnoreCase(agreementTypeId);

}

private void updateContactAndCorrespondence(MasterAgreement masterAgreement) {

//Amendment amd = masterAgreement.getLastAmendment();

if (!masterAgreement.getLinkedMaosJoin().isEmpty()) {

//UpdateBeagleOnMaosFeed.update(amd.getMaosDetail(), repository);

UpdateBeagleOnMaosFeed.updateAnAgreement(masterAgreement.getLinkedMaosJoin().iterator().next().getMaosDetails(), masterAgreement, repository);

}

}

private void setClearingHouses(List<Long> clearingHouses, MasterAgreement masterAgreement) {

Set<ClearingHouseValue> clearingHouseValues = masterAgreement.getClearingHouses();

for (Long clearingHouse : clearingHouses) {

if (clearingHouse != null) {

clearingHouseValues.add(new ClearingHouseValue(masterAgreement, clearingHouse));

}

}

}

@Override

public MasterAgreement previewGroup(AgreementType agreementType, EntityIds counterparty, EntityIds bnpParibas, List managedEntityIds, String negotiator,

Location adminLocation, Long governingLaw, String domesticBranch, Date fileOpenedDate, Long previewEntityId,

String agreementWithCSA, Long legalCounterpartyType, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, boolean doSave, List<PartyMaosDetails> maosRequests, Boolean StandardisedContract) throws InvalidMasterAgreementException {

MasterAgreement result = null;

boolean managedByCounterparty = counterparty.actingEntity != null;

boolean isFortisEntityPresentInGrp = false;

Group group = new Group();

if(!managedByCounterparty) {

List<Entity> bnpEntities = Entity.getAll(repository, managedEntityIds);

isFortisEntityPresentInGrp = bnpEntities.stream().anyMatch(bnpEntity->bnpEntity.getLegalEntityIdentifier() !=null ?

ArrayUtils.contains(Entity.getBnppFortisEntitiesLEI(), bnpEntity.getLegalEntityIdentifier().getLeiId()) : false);

}

for (Iterator iterator = managedEntityIds.iterator(); iterator.hasNext(); ) {

Long entityId = (Long) iterator.next();

if (managedByCounterparty) {

counterparty.entity = entityId;

} else {

bnpParibas.entity = entityId;

}

PartyMaosDetails maos = PartyMaosDetails.getPartyMaosDetailsByParty(maosRequests, counterparty.entity);

MasterAgreement masterAgreement = previewMasterAgreement(agreementType, counterparty, bnpParibas, negotiator, adminLocation, governingLaw, domesticBranch,

fileOpenedDate, agreementWithCSA, true, legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters,

false, maos != null ? maos.getMaosId() : null, null, null, null, null, null, StandardisedContract, null,null);

if(!managedByCounterparty) {

masterAgreement.setFortisFlag(isFortisEntityPresentInGrp);

}

masterAgreement.setGroup(group);

group.getAgreements().add(masterAgreement);

if (previewEntityId == null && result == null) {

result = masterAgreement;

} else if (managedByCounterparty && masterAgreement.getSigningEntity(Party.COUNTERPARTY).getId().equals(previewEntityId)) {

result = masterAgreement;

} else if (masterAgreement.getSigningEntity(Party.BNP_PARIBAS).getId().equals(previewEntityId)) {

result = masterAgreement;

}

}

return result;

}

@Override public MasterAgreement previewMultiMultiGroup(

AgreementType agreementType,

EntityIds counterparty,

EntityIds bnpParibas,

List cpEntities,

List bnpEntities,

String negotiator,

Location adminLocation,

Long governingLaw,

String domesticBranch,

Date fileOpenedDate,

Long previewEntityId,

String agreementWithCSA, Long legalCounterpartyType, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, boolean doSave, List<PartyMaosDetails> maosRequests, Boolean StandardisedContract) throws InvalidMasterAgreementException {

MasterAgreement result = null;

Group group = new Group();

for(Iterator iterator = cpEntities.iterator(); iterator.hasNext();){

counterparty.entity =(Long) iterator.next();

for(Iterator iterator1 = bnpEntities.iterator() ; iterator1.hasNext();){

bnpParibas.entity = (Long) iterator1.next();

PartyMaosDetails maos = PartyMaosDetails.getPartyMaosDetailsByParty(maosRequests, counterparty.entity);

MasterAgreement masterAgreement = previewMasterAgreement(agreementType, counterparty, bnpParibas, negotiator, adminLocation, governingLaw, domesticBranch,

fileOpenedDate, agreementWithCSA, true, legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters,false, maos != null ? maos.getMaosId() : null, null, null, null, null, null, StandardisedContract, null,null);

masterAgreement.setGroup(group);

group.getAgreements().add(masterAgreement);

result = masterAgreement;

}

}

return result;

}

@Override

public void deleteNegotiationItem(List documents, boolean forGroup, User user) {

if(!documents.isEmpty()){

getNegotiationService().deleteNegotiationItem(documents, forGroup, user);

}

}

@Override

public void deleteNegotiationItemForExec(List documents) {

getNegotiationService().deleteNegotiationItemForExec(documents);

}

@Override

public void deleteSharedNegotiationItemForExec(List documents) {

getNegotiationService().deleteShared NegotiationItemForExec(documents);

}

private void setBnpParibasEntities (MasterAgreement masterAgreement, EntityIds bnpParibas) {

masterAgreement.setBnpParibasEntities(

retrieveEntity(bnpParibas.entity),

retrieveEntity(bnpParibas.actingEntity),

retrieveEntity (bnpParibas.fundManagingCompany),
retrieveEntity(bnpParibas.fundWithCompartments));

}

private void setCounterpartyEntities(MasterAgreement masterAgreement, EntityIds counterparty) {

masterAgreement.setCounterpartyEntities(

retrieveEntity(counterparty.entity),

retrieveEntity(counterparty.actingEntity),

retrieveEntity(counterparty.fundManagingCompany),

retrieveEntity(counterparty.fundWithCompartments));

}

private Entity retrieveEntity(Long entityId) {

return entityId == null ? null : Entity.get(repository, entityId);

}

private void createAndAddFirstAmendment(MasterAgreement masterAgreement, Location location, String negotiator, Long governingLaw, String domesticBranch,

String agreementWithCSA, Long legalCounterpartyType, boolean doSave, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, CollateralTypeLookup collateralType, Date collNegoStartDate,

LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract, LookupItem partyPostingCollateral) {Amendment amendment = new Amendment();

Negotiator negotiatorLookup = Negotiator.find(repository, negotiator);

amendment.setAdminLocation(location);

//amendment.setNegotiator(Negotiator.find(repository, negotiator));

amendment.setNegotiator(negotiatorLookup);

masterAgreement.setGoverningLaw(repository.findGoverningLaw(governingLaw));

masterAgreement.setLegalCounterpartyType(repository.findLegalCounterpartyTypeById(legalCounterpartyType));

masterAgreement.setCloseOutNetting(closeOutNettingFlag);

masterAgreement.setTransactionSpecificMasters(transactionSpecificMasters);

NettingCalculator calculator = new NettingCalculator(masterAgreement, LegalOpinion.getLegalOpinion(repository, masterAgreement));

calculator.calculateCloseOutNettingForBNPPFortisEntities();

if (!doSave) {

masterAgreement.setTempCloseOutNetting(masterAgreement.getCloseOutNetting());

masterAgreement.setTempReasonForNonNetting(masterAgreement.getReasonForNonNetting());

masterAgreement.setCloseOutNetting(false);masterAgreement.setReasonForNonNetting(null);

} else {

if (!Boolean.TRUE.equals(masterAgreement.getFortisFlag()) && closeOutNettingFlag.equals(Boolean.FALSE)) {

masterAgreement.setCloseOutNetting(false);

masterAgreement.setReasonForNonNetting(null);

} else if (closeOutNettingFlag.equals(Boolean.TRUE)) {

masterAgreement.setCloseOutNetting(true);

masterAgreement.setReasonForNonNetting(null);

}

}

if (domesticBranch != null && !"".equals(domesticBranch)) {

amendment.setDomesticBranch(repository.getObject(DomesticBranch.class, domesticBranch));

}

if (StandardisedContract != null) {

amendment.setStandardisedContract(StandardisedContract);

}

if (masterAgreement.getAgreementType().isDerivatives() || masterAgreement.getAgreementType().isPrimeBrokerage()) {

if (AmendmentType.MA_ONLY.equals(agreementWithCSA) || (!masterAgreement.belongsToGroup() && AgreementType.getClearingTypes().contains(masterAgreement.getAgreementType().getId().getId()))) {

amendment.setType(AmendmentType.get(repository, AmendmentType.AMENDMENT_TO_MA_ONLY_ID));

amendment.setCollateralStatus(CollateralStatus.get(repository, CollateralStatus.NO_CSA_ID));

}

if (AmendmentType.MA_AND_CSA.equals(agreementWithCSA)) {

amendment.setType(AmendmentType.get(repository, AmendmentType.AMENDMENT_TO_MA_AND_COLLATERAL_ID));

masterAgreement.setCollateralFlag(true);

}

//BGL-1215 4-Feb-2010

if (masterAgreement.getAgreementType().getIsClearingAgreement() || masterAgreement.isAgreementTypeDeemedIsda()) {

final List<AdditionalTerminationEvent> terminationEvents = repository.findAdditionalTerminationEvents();

for (AdditionalTerminationEvent terminationEvent : terminationEvents) {

if ("Clearing ISDA LCH".equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId()) && terminationEvent.getId().equals(LookupItem.CLEARING_MEMBER_BNPP_ATE.getId())) {

masterAgreement.addAdditionalTerminationEvent(new TerminationEvent(masterAgreement, (Long) terminationEvent.getId(), "Y"));

}

}

}if (isMAEligileForDefaultCTD(masterAgreement)) {

final List<LookupItem> specialClauses = repository.findSpecialClauses();

for (LookupItem specialClause : specialClauses) {

if (specialClause.getId().equals(LookupItem.SPC_NEW_CTD_CODE)) {

AgreementJoin agreementJoin = AgreementHelper.getSpecialClauseInstance(masterAgreement, (Long) specialClause.getId(), true);

masterAgreement.getSpecialClauseJoins().remove(agreementJoin);

agreementJoin.setCreation(User.BEAGLE, new Date());

masterAgreement.addSpecialClause(agreementJoin);

}

}

}

}

//        amendment.join(masterAgreement, new Long(0));

masterAgreement.addAmendment(amendment);

AmendmentKey key = new AmendmentKey(masterAgreement, Long.valueOf(0));

amendment.setId(key);

//        this.id = key;

AgreementType agreementType = masterAgreement.getAgreementType();

masterAgreement.setCounterpartyCrossDefault(new CrossDefault(masterAgreement));

masterAgreement.setBnpParibasCrossDefault(new CrossDefault(masterAgreement));

if (agreementType.isDerivatives() || agreementType.isPrimeBrokerage()) {

if (amendment.getType() != null && !AmendmentType.AMENDMENT_TO_MA_ONLY_ID.equals(amendment.getType().getId())) {

masterAgreement.setCollateralData(new CollateralData(masterAgreement));

}

} else if (agreementType.isStockLending()) {

masterAgreement.setStockLending(new StockLending(masterAgreement));

} else if (agreementType.isRepurchase()) {

masterAgreement.setRepurchase(new Repurchase(masterAgreement));

}

if (collateralType != null) {

CollateralData collateralData = masterAgreement.getCollateralData();

if (collateralData != null) {

collateralData.setCollateralId(repository.fetchNextCollateralSeq());

CollateralHeaderDetails collateralHeaderDetails = getCollateralHeaderDetails(regulatoryClassification, collateralMarginType,  collateralData.getCollateralId());

collateralData.setType(collateralType);

collateralData.setAdminLocation(location);

collateralData.setNegotiator(negotiatorLookup);

collateralData.setNegotiationStartDate(collNegoStartDate);

collateralData.setCsaLawId(collateralType.getCollateralTypeLaw() != null ? collateralType.getCollateralTypeLaw().getId().toString() : null);

/* CollateralAdditionalData data = setCollateralType(masterAgreement, amendment, collateralType, 1L);

if (data != null) {

collateralData.getCollateralAdditionalDetail().add(data);

}*/

collateralData.setRegulatoryClassification(regulatoryClassification);

collateralData.setCollateralMarginType(collateralMarginType);

collateralData.setCollateralRegimeDetail(createCollateralRegimeDetail(collateralData, collateralRegime));

collateralData.setCollateralSegmentId(generateCollateralSegmentId(collateralData, collateralHeaderDetails));

collateralData.setCollateralHeaderDetails(collateralHeaderDetails);

collateralData.setPartyPostingCollateral(partyPostingCollateral);}}

if ("MSFTA".equals(masterAgreement.getAgreementType().getName())) {

masterAgreement.getRepurchase().setCurrency(Currency.find(repository, "USD"));}

}

private boolean isMAEligileForDefaultCTD(MasterAgreement ma){

return ma.getAgreementType().isDerivatives() && ((GoverningLaw) ma.getGoverningLaw()).isFrench() && LookupItem.getBnppEntityIdList().contains(ma.getBnpParibasEntity().getId());

}

@Override

public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

this.applicationContext = applicationContext;

}

private NegotiationService getNegotiationService() {

return negotiationService;

}

@Override

public MasterAgreement createAndSaveGroupDeemedISDA(AgreementType agreementType, Entity counterparty, Entity actingEntity, Entity bnpParibas,

List entityIds, String negotiator, Location adminLocation,

Long governingLaw, String domesticBranch, Date fileOpenedDate,

String agreementWithCSA, boolean createDefaultOffices, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, Long legalCounterpartyType, Date signed_on, Date agreement_date, String comments, Boolean StandardisedContract) throwsInvalidMasterAgreementException {

MasterAgreement result = null;

Group group = new Group();

repository.save(group);

LOGGER.info("Group Created with Group Id : " + group.getId());

//boolean managedByCounterparty = counterparty.actingEntity != null;

for (Iterator iterator = entityIds.iterator(); iterator.hasNext(); ) {

/* Long entityId = (Long) iterator.next();

if (managedByCounterparty) {

counterparty.entity = entityId;

} else {

bnpParibas.entity = entityId;

}*/

MasterAgreement masterAgreement = previewMasterAgreementDeemedISDA(agreementType, (Entity) iterator.next(), actingEntity, bnpParibas,

negotiator, adminLocation, governingLaw, domesticBranch, fileOpenedDate, agreementWithCSA,

createDefaultOffices, legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters, true, null, StandardisedContract);

masterAgreement.setGroup(group);

masterAgreement.setAddToGroupDate(new Date());

if (signed_on != null) {

masterAgreement.setExecutionDate(signed_on);

}Amendment amendment = masterAgreement.getAmendments().get(0);

amendment.setSignedDate(agreement_date);  //To excute the agreement

masterAgreement.setCloseOutNetting(closeOutNettingFlag);

masterAgreement.setFullTwoWayPayment(agreementType.getFullTwoWayPayment());

amendment.setComment(comments);

if(((masterAgreement.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(masterAgreement)) ||

masterAgreement.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(masterAgreement.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(masterAgreement.getGoverningLaw()).toString()) == 6 : false) {

masterAgreement.setCounterpartyAutomaticTermination(true);

masterAgreement.setBnpParibasAutomaticTermination(true);

}

repository.save(masterAgreement);

LOGGER.info("Master Agreement Created with Id : " + masterAgreement.getId() + " within Group : " + group.getId());//updateContactAndCorrespondence(masterAgreement);

if (result == null) {

result = masterAgreement;

}

setSftrFlagForCreateMa(masterAgreement);

}

return result;

}

@Override

public MasterAgreement previewMasterAgreementDeemedISDA(AgreementType agreementType,

Entity counterparty, Entity actingEntity, Entity bnpParibas,

String negotiator,

Location adminLocation, Long governingLaw, String domesticBranch,

Date fileOpenedDate,

String agreementWithCSA,

boolean createDefaultOffices, Long legalCounterpartyType, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, boolean doSave, Long maosRequestId, Boolean StandardisedContract) throws InvalidMasterAgreementException {

MasterAgreement masterAgreement = new MasterAgreement();

masterAgreement.setAgreementType(agreementType);

//setCounterpartyEntities(masterAgreement, counterparty);

masterAgreement.setCounterpartyEntities(counterparty, actingEntity, null, null);

//setBnpParibasEntities(masterAgreement, bnpParibas);

masterAgreement.setBnpParibasEntity(bnpParibas);

masterAgreement.setFileOpenedDate(fileOpenedDate);

masterAgreement.setWithCSA(AmendmentType.hasCSA(agreementWithCSA));

/*Long entityId = bnpParibas.actingEntity;

if (entityId == null) {

entityId = bnpParibas.entity;

}

Entity bnpEntity = Entity.get(repository, entityId);

if (!doSave) {

boolean contains = ArrayUtils.contains(Entity.BNPP_FORTIS_ENTITY_IDS, bnpEntity.getId()) || ArrayUtils.contains(Entity.BNPP_FORTIS_ENTITIES, bnpEntity.getRmpmGroupCode());

masterAgreement.setFortisFlag(Boolean.valueOf(contains));

} else {

masterAgreement.setFortisFlag(fortisFlag == null ? Boolean.FALSE : fortisFlag);

}*/

masterAgreement.setFortisFlag(Boolean.FALSE);

createAndAddFirstAmendment(masterAgreement, adminLocation, negotiator, governingLaw, domesticBranch, agreementWithCSA,

legalCounterpartyType, doSave, closeOutNettingFlag, transactionSpecificMasters, null, null, null, null, null, StandardisedContract, null);

masterAgreement.setSigningAuthority("N");

if (createDefaultOffices) {

AgreementHelper.createDefaultOffices(masterAgreement, repository);

}

if (maosRequestId != null) {

MaosDetails maosDetails = repository.getObject(MaosDetails.class, maosRequestId);

if (maosDetails != null) {

MaosIdJoin maosIdJoin = new MaosIdJoin(masterAgreement, maosDetails);

masterAgreement.setLinkedMaosJoin(maosIdJoin);

maosDetails.getMaosLinkedMAs().add(maosIdJoin);

}

}

masterAgreement.validateEntities();

masterAgreement.setInitialDefaults();

NettingCalculator calculator = new NettingCalculator(masterAgreement, LegalOpinion.getLegalOpinion(repository, masterAgreement));

calculator.calculateCoveredProductNetting(true);

return masterAgreement;

}

@Override

public void updateEmailForNoRelation() {

//if (!checkIfJobIsExeOnAnyNode(NO_REL_MA_JOB_ID)) {

makeJobExecutionEntry(node, NO_REL_MA_JOB_ID, NO_REL_MA_JOB_NAME, NO_REL_MA_JOB_DESC);

makeJobExecutionEntry(node, NO_REL_MA_JOB_ID, NO_REL_MA_JOB_NAME, NO_REL_MA_JOB_DESC);

LOGGER.info("Started Job execution for - Mail for No Relation on Master agreements on " + node);

draftStatusUpdateEmail("dateOfNoRelation", "[CANCEL] NORL Event", ALL, "norl_notification.vm", "No Relation on Master agreements - ", "norlRelations", BeagleDateUtils.addToSystemTime(0, -30, 0, 0));

LOGGER.info("Completed Job execution for - Mail for No Relation on Master agreements on " +node);

}

// }

@Override

@Transactional

public void generateTableuXml() {

if (!checkIfJobIsExeOnAnyNode(TABLEU_DAILY_XML)) {

try {

makeJobExecutionEntry(node, TABLEU_DAILY_XML, TABLEU_DAILY_XML_NAME, TABLEU_DAILY_XML_DESC);

String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

String fileName=getTableuXmlOutputPath()+"PBA_Key_terms_"+date+".xml";

LOGGER.info("Started Job execution for - Send Daily XML to tableu " + node);

List pbaAgreementsIdsWithRestrictedClient = repository.getAllPbaAgreementsWithRestrictedClient();List<Long> l= new ArrayList<>();

for(Object o : pbaAgreementsIdsWithRestrictedClient){

l.add(Long.valueOf(o.toString()));

}

List<MasterAgreement> pbaAgreementsWithRestrictedClient = repository.loadAllAgreementById(l);

TableuMessage tableuMessage = new TableuMessage();

List<IndividualAgreementInTableuMessage> finalListOfMaWithInfo = new ArrayList<>();

for(MasterAgreement ma : pbaAgreementsWithRestrictedClient){

IndividualAgreementInTableuMessage individualAgreementInTableuMessage = new IndividualAgreementInTableuMessage(ma.getId(),ma.getGrp_id(),ma.getStatus().toString(),ma.getAgreementType().toString(),ma.getAgreementType().getCategory()

,ma.getAgreementType().getFamilyId(),ma.getGoverningLaw().getText(),ma.getExecutionDate().toString());

setEntitiesForTableuMsg(ma,individualAgreementInTableuMessage);

setAffiliateList(ma,individualAgreementInTableuMessage);

setMarginMaintenance(ma,individualAgreementInTableuMessage);

setMarginExcessReturn(ma,individualAgreementInTableuMessage);

setFinancing(ma,individualAgreementInTableuMessage);

setEventsOfDefault( ma,  individualAgreementInTableuMessage);

setFailureToPayPostMargin(ma,individualAgreementInTableuMessage);

setInternalCrossDefault(ma,individualAgreementInTableuMessage);

setExternalCrossDefault(ma,individualAgreementInTableuMessage);

setMiscelleneousDetails(ma,individualAgreementInTableuMessage);

finalListOfMaWithInfo.add(individualAgreementInTableuMessage);

}

tableuMessage.setAgreementList(finalListOfMaWithInfo);

XStream xstream = new XStream();

xstream.aliasSystemAttribute(null, "class");

xstream.processAnnotations(TableuMessage.class);

String finalStatus = xstream.toXML(tableuMessage);

Path fileToCreate = Paths.get(fileName);

Files.createFile(fileToCreate);

FileWriter fileWriter = new FileWriter(fileName);

fileWriter.write(finalStatus);

fileWriter.close();

LOGGER.info("Completed Job execution for - Send Daily XML to tableu " +node);

} catch (Exception ex) {

LOGGER.error("Error while resetting running jobs", ex);

}

}

}

@Override

@Transactional

public void generateTableuXmlForLUA() {

if (!checkIfJobIsExeOnAnyNode(TABLEU_LUA_DAILY_XML)) {

try {

makeJobExecutionEntry(node, TABLEU_LUA_DAILY_XML, TABLEU_LUA_DAILY_XML_NAME, TABLEU_LUA_DAILY_XML_DESC);

String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

String fileName = getTableuXmlOutputPath() + "LUA_Key_terms_" + date + ".xml";

LOGGER.info("Started Job execution for - Send Daily XML to tableu for LUA " + node);

List luaAgreementsIdsWithRestrictedClient = repository.getAllLuaAgreementsWithRestrictedClient();

List<Long> l = new ArrayList<>();

for (Object o : luaAgreementsIdsWithRestrictedClient) {

l.add(Long.valueOf(o.toString()));

}

try {

List<MasterAgreement> luaAgreementsWithRestrictedClient = new ArrayList<>();

luaAgreementsWithRestrictedClient = repository.loadAllAgreementById(l);

LUATableuMessage luatableuMessage = new LUATableuMessage();

List<IndividualLUAAgreementInTableuMessage> finalListOfMaWithInfo = new ArrayList<>();

for (MasterAgreement ma : luaAgreementsWithRestrictedClient) {

IndividualLUAAgreementInTableuMessage individualluaAgreementInTableuMessage = new IndividualLUAAgreementInTableuMessage(ma.getId(), ma.getGroup() != null ? ma.getGroup().getId() : null, ma.getStatus().toString(), ma.getAgreementType().toString(), ma.getAgreementType().getCategory()

, ma.getAgreementType().getFamilyId(), ma.getGoverningLaw().getText(), ma.getExecutionDate().toString());

individualluaAgreementInTableuMessage.setLuakeyterms(new LuaKeyTerms());

setLUAEntitiesForTableuMsg(ma, individualluaAgreementInTableuMessage);

setLUAMiscellaneousSections(ma, individualluaAgreementInTableuMessage);

setLUAEventsOfDefaultSection(ma, individualluaAgreementInTableuMessage);

setLUALockupTermSection(ma, individualluaAgreementInTableuMessage);

setLUACoveredProducts(ma, individualluaAgreementInTableuMessage);

setLUAScopeofCommitment(ma, individualluaAgreementInTableuMessage);

setLUALiquidityCoverageRatio(ma, individualluaAgreementInTableuMessage);

setLUANavTrigFlrSection(ma, individualluaAgreementInTableuMessage);

setLUANavTrigTermPerdSection(ma, individualluaAgreementInTableuMessage);

setLUAPreNoticeFinanSection(ma, individualluaAgreementInTableuMessage);

setLUAPostNoticeFinanSection(ma, individualluaAgreementInTableuMessage);

setLUAFishCutBaitSection(ma, individualluaAgreementInTableuMessage);

setLUAMarginMaintenanceSection(ma, individualluaAgreementInTableuMessage);

finalListOfMaWithInfo.add(individualluaAgreementInTableuMessage);

}

luatableuMessage.setAgreementList(finalListOfMaWithInfo);

XStream xstream = new XStream();

xstream.aliasSystemAttribute(null, "class");

xstream.processAnnotations(LUATableuMessage.class);

String finalStatus = xstream.toXML(luatableuMessage);

Path fileToCreate = Paths.get(fileName);

Files.createFile(fileToCreate);

FileWriter fileWriter = new FileWriter(fileName);

fileWriter.write(finalStatus);

fileWriter.close();

LOGGER.info("Completed Job execution for - Send Daily XML to tableu " + node);

} catch (Exception e) {

LOGGER.error("Error while setting values in XML for LUA Key Terms Tab", e);

}

} catch (Exception ex) {

LOGGER.error("Error while resetting running jobs", ex);

}

}

}

public void setMiscelleneousDetails(MasterAgreement ma, IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

PostDefaultRemedies postDefaultRemedies=new PostDefaultRemedies("","","");

SetOff setOff=new SetOff("","");

PrimeBrokerLiabilities primeBrokerLiabilities=new PrimeBrokerLiabilities("","");

individualAgreementInTableuMessage.setAdditionalComments("");

if(ma.getPbaKeyTermMiscellaneousSections()!=null) {

postDefaultRemedies = new PostDefaultRemedies(StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getDefRemTrigger())?"":ma.getPbaKeyTermMiscellaneousSections().getDefRemTrigger(),

StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getDefRemNotifRequired())?"":ma.getPbaKeyTermMiscellaneousSections().getDefRemNotifRequired(),

StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getDefRemNotifType())?"":ma.getPbaKeyTermMiscellaneousSections().getDefRemNotifType());

setOff = new SetOff(StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getSetOffNotifType())?"":ma.getPbaKeyTermMiscellaneousSections().getSetOffNotifType(),

StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getSetOffRights())?"":ma.getPbaKeyTermMiscellaneousSections().getSetOffRights());

primeBrokerLiabilities = new PrimeBrokerLiabilities(StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getPbLiabStd())?"":ma.getPbaKeyTermMiscellaneousSections().getPbLiabStd(),

StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getPbLiabResponseType())?"":ma.getPbaKeyTermMiscellaneousSections().getPbLiabResponseType());

individualAgreementInTableuMessage.setAdditionalComments(StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getComments())?"":ma.getPbaKeyTermMiscellaneousSections().getComments());

}

List<FishOrCutBaitTableu> fishOrCutBaitList = new ArrayList<>();

for(FishOrCutBait fishOrCutBait : ma.getFishOrCutBaitList()){

FishOrCutBaitTableu fishOrCutBaitTableu =  new FishOrCutBaitTableu(StringUtils.isBlank(fishOrCutBait.getFocbApplicable())?"":fishOrCutBait.getFocbApplicable(),

StringUtils.isBlank(fishOrCutBait.getAllEventsOfDefault())?"":fishOrCutBait.getAllEventsOfDefault(),

StringUtils.isBlank(fishOrCutBait.getAllTerminationEvents())?"":fishOrCutBait.getAllTerminationEvents(),

fishOrCutBait.getDeemedWaiverDays()!=null?fishOrCutBait.getDeemedWaiverDays().toString():"",

StringUtils.isBlank(fishOrCutBait.getDeemedWaiverDayType())?"":fishOrCutBait.getDeemedWaiverDayType());

fishOrCutBaitList.add(fishOrCutBaitTableu);

}

if(ma.getFishOrCutBaitList().isEmpty()){

FishOrCutBaitTableu fishOrCutBaitTableu = new FishOrCutBaitTableu("","","","","");

fishOrCutBaitList.add(fishOrCutBaitTableu);

}

individualAgreementInTableuMessage.setFishOrCutBait(fishOrCutBaitList);

individualAgreementInTableuMessage.setPrimeBrokerLiabilities(primeBrokerLiabilities);

individualAgreementInTableuMessage.setSetOff(setOff);

individualAgreementInTableuMessage.setPostDefaultRemedies(postDefaultRemedies);

}

public void setInternalCrossDefault(MasterAgreement ma,IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

List<InternalCrossDefaultTableu> internalCrossDefaultTableuList = new ArrayList<>();

for(InternalCrossDefault internalCrossDefault : ma.getInternalCrossDefaults()){

AgreementsInScope agreementsInScope = new AgreementsInScope(internalCrossDefault.getInternalCrossDefaultAgreements().stream().map(x-> x.getAgreementInScope().getEnumValue()).collect(Collectors.toList()));

EntitiesInScope entitiesInScope = new EntitiesInScope(internalCrossDefault.getInternalCrossDefaultEntities().stream().map(x-> x.getEntityInScope().getEnumValue()).collect(Collectors.toList()));

CrossDefaultFailureType crossDefaultFailureType = new CrossDefaultFailureType(internalCrossDefault.getInternalCrossDefaultFailureTypes().stream().map(x-> x.getFailureType().getEnumValue()).collect(Collectors.toList()));

InternalCrossDefaultTableu internalCrossDefaultTableu = new InternalCrossDefaultTableu(StringUtils.isBlank(internalCrossDefault.getBnpApplicability())?"":internalCrossDefault.getBnpApplicability(),

StringUtils.isBlank(internalCrossDefault.getClientApplicability())?"":internalCrossDefault.getClientApplicability(),StringUtils.isBlank(internalCrossDefault.getCrossDefaultStandard())?"":internalCrossDefault.getCrossDefaultStandard(),

crossDefaultFailureType.getCrossdefaultfailuretypes().isEmpty()? new CrossDefaultFailureType(Arrays.asList("")):crossDefaultFailureType,

entitiesInScope.getEntitiesinscope().isEmpty()?new EntitiesInScope(Arrays.asList("")):entitiesInScope,

agreementsInScope.getAgreementsinscope().isEmpty()?new AgreementsInScope(Arrays.asList("")):agreementsInScope);

internalCrossDefaultTableuList.add(internalCrossDefaultTableu);

}

if(ma.getInternalCrossDefaults().isEmpty()){

InternalCrossDefaultTableu internalCrossDefaultTableu = new InternalCrossDefaultTableu("","","",

new CrossDefaultFailureType(Arrays.asList("")),

new EntitiesInScope(Arrays.asList("")),

new AgreementsInScope(Arrays.asList("")));

internalCrossDefaultTableuList.add(internalCrossDefaultTableu);

}

individualAgreementInTableuMessage.setInternalcrossdefaultlist(internalCrossDefaultTableuList);

}

public void setExternalCrossDefault(MasterAgreement ma,IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

List<ExternalCrossDefaultTableu> externalCrossDefaultTableuList = new ArrayList<>();

for(ExternalCrossDefault externalCrossDefault : ma.getExternalCrossDefaults()){

CrossDefaultFailureType crossDefaultFailureType = new CrossDefaultFailureType(externalCrossDefault.getExternalCrossDefaultFailureTypes().stream().map(x-> x.getFailureType().getEnumValue()).collect(Collectors.toList()));

ExternalCrossDefaultTableu externalCrossDefaultTableu = new ExternalCrossDefaultTableu(StringUtils.isBlank(externalCrossDefault.getBnpApplicability())?"":externalCrossDefault.getBnpApplicability(),

StringUtils.isBlank(externalCrossDefault.getClientApplicability())?"":externalCrossDefault.getClientApplicability(),

StringUtils.isBlank(externalCrossDefault.getCrossDefaultStandard())?"":externalCrossDefault.getCrossDefaultStandard(),crossDefaultFailureType.getCrossdefaultfailuretypes().isEmpty()? new CrossDefaultFailureType(Arrays.asList("")):crossDefaultFailureType,

StringUtils.isBlank(externalCrossDefault.getThresholdApplicable())?"":externalCrossDefault.getThresholdApplicable());

externalCrossDefaultTableuList.add(externalCrossDefaultTableu);

}

if(ma.getExternalCrossDefaults().isEmpty()){

ExternalCrossDefaultTableu externalCrossDefaultTableu = new ExternalCrossDefaultTableu("","","",

new CrossDefaultFailureType(Arrays.asList("")),"");

externalCrossDefaultTableuList.add(externalCrossDefaultTableu);

}

individualAgreementInTableuMessage.setExternalcrossdefaultlist(externalCrossDefaultTableuList);

}

public void setFailureToPayPostMargin(MasterAgreement ma, IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

List<FailureToPayPostMarginTableu> failureToPayPostMarginTableuList = new ArrayList<>();

for(FailureToPayPostMargin failureToPayPostMargin : ma.getFailureToPayPostMarginList()){

EventType eventType = new EventType(Arrays.asList(failureToPayPostMargin.getFailureToPayPostMarginEventTypeValues().split(",")));

FailureToPayPostMarginTableu failureToPayPostMarginTableu = new FailureToPayPostMarginTableu(StringUtils.isBlank(failureToPayPostMargin.getClientApplicable())?"":failureToPayPostMargin.getClientApplicable(),

eventType,

StringUtils.isBlank(failureToPayPostMargin.getCureType())?"":failureToPayPostMargin.getCureType(),

StringUtils.isBlank(failureToPayPostMargin.getAdmErrorCurePeriodTimeLoc())?"":failureToPayPostMargin.getAdmErrorCurePeriodTimeLoc(),

StringUtils.isBlank(failureToPayPostMargin.getAdmErrorCurePeriodTiming())?"":failureToPayPostMargin.getAdmErrorCurePeriodTiming(),

StringUtils.isBlank(failureToPayPostMargin.getCureCondition())?"":failureToPayPostMargin.getCureCondition(),

StringUtils.isBlank(failureToPayPostMargin.getCurePeriodDayType())?"":failureToPayPostMargin.getCurePeriodDayType());

failureToPayPostMarginTableuList.add(failureToPayPostMarginTableu);

}if(ma.getFailureToPayPostMarginList().isEmpty()){

FailureToPayPostMarginTableu failureToPayPostMarginTableu = new FailureToPayPostMarginTableu("",new EventType(Arrays.asList("")),"","","","","");

failureToPayPostMarginTableuList.add(failureToPayPostMarginTableu);

}

individualAgreementInTableuMessage.setFailureToPayPostMarginList(failureToPayPostMarginTableuList);

}

public void setEventsOfDefault(MasterAgreement ma, IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

List<EventOfDefaultTableu> finalList = new ArrayList<>();

for(EventOfDefault eventOfDefault : ma.getEventsOfDefaultList()) {

EventOfDefaultTableu eventOfDefaultTableu = new EventOfDefaultTableu(eventOfDefault.getBnppApplicability()!=null ? eventOfDefault.getBnppApplicability().getEnumValue():"",

eventOfDefault.getClientApplicability()!=null ?eventOfDefault.getClientApplicability().getEnumValue():"",

eventOfDefault.getDefaultEventType()!=null ? eventOfDefault.getDefaultEventType().getEnumValue():"",eventOfDefault.getGracePeriodDay()!=null?eventOfDefault.getGracePeriodDay().toString():"",

eventOfDefault.getGracePeriodDayRef()!=null?eventOfDefault.getGracePeriodDayRef().getEnumValue():"",

eventOfDefault.getMaterialityQualifier()!=null?eventOfDefault.getMaterialityQualifier().getEnumValue():"");

finalList.add(eventOfDefaultTableu);

}

if(ma.getEventsOfDefaultList().isEmpty()){

EventOfDefaultTableu eventOfDefaultTableu = new EventOfDefaultTableu("","","","","","");

finalList.add(eventOfDefaultTableu);

}

individualAgreementInTableuMessage.setEventOfDefaultList(finalList);

}

public void setFinancing(MasterAgreement ma, IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

if(ma.getPbaKeyTermMiscellaneousSections()!=null && !StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getFinRepayOfCashLoan())){

individualAgreementInTableuMessage.setFinancing(ma.getPbaKeyTermMiscellaneousSections().getFinRepayOfCashLoan());

}else{

individualAgreementInTableuMessage.setFinancing("");

}

}

public void setMarginExcessReturn(MasterAgreement ma, IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

if(ma.getPbaKeyTermMiscellaneousSections()!=null){

MarginExcessReturn  marginExcessReturn = new MarginExcessReturn(StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getMarginExcRetFailConseq())?"":ma.getPbaKeyTermMiscellaneousSections().getMarginExcRetFailConseq()

,StringUtils.isBlank(ma.getPbaKeyTermMiscellaneousSections().getMarginExcRetObligation())?"":ma.getPbaKeyTermMiscellaneousSections().getMarginExcRetObligation());

individualAgreementInTableuMessage.setMarginExcessReturnList(marginExcessReturn);

}else{

individualAgreementInTableuMessage.setMarginExcessReturnList(new MarginExcessReturn("",""));

}

}

public void setMarginMaintenance(MasterAgreement ma , IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

List<MarginMaintenanceTableu> finalMarginMaintenacneList =new ArrayList<>();

for(MarginMaintenance marginMaintenance : ma.getMarginMaintenanceList()){

MarginMaintenanceTableu maintenance = new MarginMaintenanceTableu(StringUtils.isBlank(marginMaintenance.getMarginNotificationTimePeriod())?"":marginMaintenance.getMarginNotificationTimePeriod(),

StringUtils.isBlank(marginMaintenance.getMarginNotificationTime())?"":marginMaintenance.getMarginNotificationTime(),

StringUtils.isBlank(marginMaintenance.getMarginNotificationTimeLocation())?"":marginMaintenance.getMarginNotificationTimeLocation(),

StringUtils.isBlank(marginMaintenance.getDayType())?"":marginMaintenance.getDayType(),

StringUtils.isBlank(marginMaintenance.getMarginTranferTimePeriod())?"":marginMaintenance.getMarginTranferTimePeriod(),

StringUtils.isBlank(marginMaintenance.getMarginTranferTiming())?"":marginMaintenance.getMarginTranferTiming(), 

StringUtils.isBlank(marginMaintenance.getMarginTransferTime())?"":marginMaintenance.getMarginTransferTime(), 

StringUtils.isBlank(marginMaintenance.getMarginTransferTimeLocation())?"":marginMaintenance.getMarginTransferTimeLocation());

finalMarginMaintenacneList.add(maintenance);

}

if(ma.getMarginMaintenanceList().isEmpty()){

MarginMaintenanceTableu marginMaintenanceTableu = new MarginMaintenanceTableu("","","","","","","","");

finalMarginMaintenacneList.add(marginMaintenanceTableu);

}

individualAgreementInTableuMessage.setMarginmaintenance(finalMarginMaintenacneList);

}

public void setAffiliateList(MasterAgreement ma ,IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

Affiliate finalAffiliate = new Affiliate();

List<String> tempAffiliateList = new ArrayList<>();

for(AffiliatesPbaKeyTerm a : ma.getAffiliatesPbaKeyTermJoins() ){

tempAffiliateList.add(a.getPbaKeyTermEnum().getEnumValue());

}

if(ma.getAffiliatesPbaKeyTermJoins().isEmpty()){

tempAffiliateList.add("");

}

finalAffiliate.setAffiliate(tempAffiliateList);

individualAgreementInTableuMessage.setAffiliateList(finalAffiliate);

}

public void setEntitiesForTableuMsg(MasterAgreement ma ,IndividualAgreementInTableuMessage individualAgreementInTableuMessage){

EntityBasicInfo cp = new EntityBasicInfo(ma.getCounterpartyEntity().getMkpCode(),ma.getCounterpartyEntity().getLocation().getCode(),ma.getCounterpartyEntity().getEntityCode());

EntityBasicInfo bnp = new EntityBasicInfo(ma.getBnpParibasEntity().getMkpCode(),ma.getBnpParibasEntity().getLocation().getCode(),ma.getBnpParibasEntity().getEntityCode());

EntityBasicInfo cpActing=null;

if(ma.getCounterpartyIsManaged())

cpActing = new EntityBasicInfo(ma.getActingEntity(Party.COUNTERPARTY).getMkpCode(),ma.getActingEntity(Party.COUNTERPARTY).getLocation().getCode(),ma.getActingEntity(Party.COUNTERPARTY).getEntityCode());

Counterparty counterparty = new Counterparty(cp,ma.getCounterpartyIsManaged()?cpActing:null);

Bnp bnpParty = new Bnp(bnp);

individualAgreementInTableuMessage.setEntityBasicInfoCp(counterparty);

individualAgreementInTableuMessage.setEntityBasicInfoBnp(bnpParty);

}

public void setLUAMiscellaneousSections(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

LengthOfLockupTableuForLUA lengthOfLockup = new LengthOfLockupTableuForLUA("","");

FundingEventTableuForLUA fundingEvent =new FundingEventTableuForLUA("","");

MarginExcessReturnForLUA marginExcessReturnForLUA = new MarginExcessReturnForLUA("");

individualAgreementInTableuMessage.getLuakeyterms().setAdditionalComments("");

if(ma.getLuaKeyTermMiscellaneousSections()!=null) {

lengthOfLockup = new LengthOfLockupTableuForLUA(ma.getLuaKeyTermMiscellaneousSections().getNoticePeriodDays() !=null ? ma.getLuaKeyTermMiscellaneousSections().getNoticePeriodDays().toString() : "",

StringUtils.isBlank(ma.getLuaKeyTermMiscellaneousSections().getNoticePeriodDayType())?"":ma.getLuaKeyTermMiscellaneousSections().getNoticePeriodDayType());

fundingEvent =new FundingEventTableuForLUA(StringUtils.isBlank(ma.getLuaKeyTermMiscellaneousSections().getLeadingToPriceChange())?"":ma.getLuaKeyTermMiscellaneousSections().getLeadingToPriceChange(),

StringUtils.isBlank(ma.getLuaKeyTermMiscellaneousSections().getLeadingToTermination())?"":ma.getLuaKeyTermMiscellaneousSections().getLeadingToTermination());

marginExcessReturnForLUA = new MarginExcessReturnForLUA(StringUtils.isBlank(ma.getLuaKeyTermMiscellaneousSections().getMarginexcess_Return_Collateral_Type())?"":ma.getLuaKeyTermMiscellaneousSections().getMarginexcess_Return_Collateral_Type());

individualAgreementInTableuMessage.getLuakeyterms().setAdditionalComments(StringUtils.isBlank(ma.getLuaKeyTermMiscellaneousSections().getComments())?"":ma.getLuaKeyTermMiscellaneousSections().getComments());

}

individualAgreementInTableuMessage.getLuakeyterms().setLengthoflockup(lengthOfLockup);

individualAgreementInTableuMessage.getLuakeyterms().setFundingEvent(fundingEvent);individualAgreementInTableuMessage.getLuakeyterms().setMarginExcessReturnList(marginExcessReturnForLUA);

}

public void setLUAEventsOfDefaultSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<EventsOfDefaultlistTableuForLUA> finalEventDefaultList =new ArrayList<>();

for(LuaEventOfDefaultSection eventsOfDefault : ma.getLuaEventOfDefaultSection()){

Conditions conditions = new Conditions(eventsOfDefault.getLuaEventOfDefaultCondition().stream().map(x-> x.getLuaKeyTermEnum().getEnumValue()).collect(Collectors.toList()));

EventsOfDefaultlistTableuForLUA eventsOfDefaultObj = new EventsOfDefaultlistTableuForLUA(StringUtils.isBlank(eventsOfDefault.getClientApplicability())?"":eventsOfDefault.getClientApplicability(),

StringUtils.isBlank(eventsOfDefault.getDefaultEventType())?"":eventsOfDefault.getDefaultEventType(),

eventsOfDefault.getGracePeriodDay() != null ? eventsOfDefault.getGracePeriodDay().toString() : "",StringUtils.isBlank(eventsOfDefault.getGracePeriodDayRef())?"":eventsOfDefault.getGracePeriodDayRef(),

conditions.getConditions().isEmpty()? new Conditions(Arrays.asList("")):conditions);

finalEventDefaultList.add(eventsOfDefaultObj);

}

if(ma.getLuaEventOfDefaultSection().isEmpty()){

EventsOfDefaultlistTableuForLUA eventOfDefault = new EventsOfDefaultlistTableuForLUA("","","","",new Conditions(Arrays.asList("")));

finalEventDefaultList.add(eventOfDefault);

}

individualAgreementInTableuMessage.getLuakeyterms().setEventsOfDefaultList(finalEventDefaultList);

}

public void setLUALockupTermSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<LockupTerminationEventslistTableuForLUA> finalLockupTermList =new ArrayList<>();

for(LuaLockUpTerminationEvent lockUpTermEventObj : ma.getLuaLockUpTerminationEvent()){

LockupTerminationEventslistTableuForLUA lockUpTermEvent = new LockupTerminationEventslistTableuForLUA(StringUtils.isBlank(lockUpTermEventObj.getEventType())?"":lockUpTermEventObj.getEventType(),

lockUpTermEventObj.getCurePeriod()!=null ? lockUpTermEventObj.getCurePeriod().toString() : "",

StringUtils.isBlank(lockUpTermEventObj.getCurePeriodType())?"":lockUpTermEventObj.getCurePeriodType());

finalLockupTermList.add(lockUpTermEvent);

}

if(ma.getLuaLockUpTerminationEvent().isEmpty()){

LockupTerminationEventslistTableuForLUA lockUpTermEvent = new LockupTerminationEventslistTableuForLUA("","","");

finalLockupTermList.add(lockUpTermEvent);

}

individualAgreementInTableuMessage.getLuakeyterms().setLockupterminationeventslist(finalLockupTermList);

}

public void setLUACoveredProducts(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

CoveredProductsListTableuForLUA coveredProductsListTableuForLUA =new CoveredProductsListTableuForLUA();

List<String> finalCoveredProductsList =new ArrayList<>();

for(LuaCoveredProduct coveredProductObj : ma.getLuaCoveredProduct()){

finalCoveredProductsList.add(StringUtils.isBlank(coveredProductObj.getProductType())?"":coveredProductObj.getProductType());

}

if(ma.getLuaCoveredProduct().isEmpty()){

finalCoveredProductsList.add("");

}

coveredProductsListTableuForLUA.setProductType(finalCoveredProductsList);

individualAgreementInTableuMessage.getLuakeyterms().setCoveredproductsList(coveredProductsListTableuForLUA);

}

public void setLUAScopeofCommitment(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

ScopeOfCommitmentTableuForLUA scopeOfCommitmentTableuForLUA = new ScopeOfCommitmentTableuForLUA();

List<String> finalScopeofCommitmentList =new ArrayList<>();

for(LuaScopeOfCommitment scopeofCommitmentObj : ma.getLuaScopeOfCommitment()){

finalScopeofCommitmentList.add(StringUtils.isBlank(scopeofCommitmentObj.getModificationMethd())?"":scopeofCommitmentObj.getModificationMethd());

}if(ma.getLuaScopeOfCommitment().isEmpty()){

finalScopeofCommitmentList.add("");

}

scopeOfCommitmentTableuForLUA.setModificationmethod(finalScopeofCommitmentList);

individualAgreementInTableuMessage.getLuakeyterms().setScopeofcommitment(scopeOfCommitmentTableuForLUA);

}

public void setLUALiquidityCoverageRatio(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

LiquidityCoverageRatioTableuForLUA liquidityCoverageRatioTableuForLUA = new LiquidityCoverageRatioTableuForLUA();

List<String> finalLiqCovRatioList =new ArrayList<>();

for(LuaLiquidityCoverageRatio liquidityCoverageRatioObj : ma.getLuaLiquidityCoverageRatio()){

finalLiqCovRatioList.add(StringUtils.isBlank(liquidityCoverageRatioObj.getLcrMethod())?"":liquidityCoverageRatioObj.getLcrMethod());

}

if(ma.getLuaScopeOfCommitment().isEmpty()){

finalLiqCovRatioList.add("");

}

liquidityCoverageRatioTableuForLUA.setLcrmethod(finalLiqCovRatioList);

individualAgreementInTableuMessage.getLuakeyterms().setLiquidityCoverageRatio(liquidityCoverageRatioTableuForLUA);

}

public void setLUANavTrigFlrSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<NavTriggersFloorlistTableuForLUA> finalNavTrigFlrList =new ArrayList<>();

for(LuaNavTriggerFloor navTrigFlrObj : ma.getLuaNavTriggerFloor()){

NavTriggersFloorlistTableuForLUA navtrigflr = new NavTriggersFloorlistTableuForLUA(StringUtils.isBlank(navTrigFlrObj.getAffectedParty())?"":navTrigFlrObj.getAffectedParty(),

StringUtils.isBlank(navTrigFlrObj.getNavComparision())?"":navTrigFlrObj.getNavComparision(),

navTrigFlrObj.getFloorAmount() != null ? navTrigFlrObj.getFloorAmount().toString() : "",

StringUtils.isBlank(navTrigFlrObj.getFloorAmountCurrency())?"":navTrigFlrObj.getFloorAmountCurrency(),

StringUtils.isBlank(navTrigFlrObj.getFloorAmountMethod())?"":navTrigFlrObj.getFloorAmountMethod(),

StringUtils.isBlank(navTrigFlrObj.getFloorDeclineType())?"":navTrigFlrObj.getFloorDeclineType(),

StringUtils.isBlank(navTrigFlrObj.getTriggerDeterminationDate())?"":navTrigFlrObj.getTriggerDeterminationDate(),

StringUtils.isBlank(navTrigFlrObj.getTriggerDeterminationDateLookBack())?"":navTrigFlrObj.getTriggerDeterminationDateLookBack(),

StringUtils.isBlank(navTrigFlrObj.getNavTermComplex())?"":navTrigFlrObj.getNavTermComplex());

finalNavTrigFlrList.add(navtrigflr);

}

if(ma.getLuaNavTriggerFloor().isEmpty()){

NavTriggersFloorlistTableuForLUA navTrigFlr = new NavTriggersFloorlistTableuForLUA("","","","","","","","","");

finalNavTrigFlrList.add(navTrigFlr);

}

individualAgreementInTableuMessage.getLuakeyterms().setNavtriggersfloorlist(finalNavTrigFlrList);

}

public void setLUANavTrigTermPerdSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<NavTriggerPeriodslistTableuForLUA> finalNavTrigPeriodList =new ArrayList<>();

for(LuaNavTriggerPeriod navTrigPrdObj : ma.getLuaNavTriggerPeriod()){

NavTriggerPeriodslistTableuForLUA navtrigprd = new NavTriggerPeriodslistTableuForLUA(StringUtils.isBlank(navTrigPrdObj.getAffectedParty())?"":navTrigPrdObj.getAffectedParty(),

navTrigPrdObj.getDeclinePercentage() != null && !StringUtils.isBlank(navTrigPrdObj.getDeclinePercentage().toString()) ? navTrigPrdObj.getDeclinePercentage().toString() : "",

StringUtils.isBlank(navTrigPrdObj.getDeclineType())?"":navTrigPrdObj.getDeclineType(),

StringUtils.isBlank(navTrigPrdObj.getNavTriggerType())?"":navTrigPrdObj.getNavTriggerType(),

StringUtils.isBlank(navTrigPrdObj.getTriggerDeterminationDate())?"":navTrigPrdObj.getTriggerDeterminationDate(),

StringUtils.isBlank(navTrigPrdObj.getTriggerDeterminationDateLookBack())?"":navTrigPrdObj.getTriggerDeterminationDateLookBack(),

StringUtils.isBlank(navTrigPrdObj.getNavTermComplex())?"":navTrigPrdObj.getNavTermComplex());

finalNavTrigPeriodList.add(navtrigprd);

}if(ma.getLuaNavTriggerPeriod().isEmpty()){

NavTriggerPeriodslistTableuForLUA navTrigTermPrd = new NavTriggerPeriodslistTableuForLUA("","","","","","","");

finalNavTrigPeriodList.add(navTrigTermPrd);

}

individualAgreementInTableuMessage.getLuakeyterms().setNavtriggerperiodslist(finalNavTrigPeriodList);

}

public void setLUAPreNoticeFinanSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<PreNoticeFinancingCaplistTableuForLUA> finalPreNoticeList =new ArrayList<>();

for(LuaPrePostNoticeFinancingCap preNoticeFinanObj : ma.getLuaPrePostNoticeFinancingCap()){

if(null!=preNoticeFinanObj.getPreOrPost() && preNoticeFinanObj.getPreOrPost().equalsIgnoreCase("PRE")) {

PreNoticeFinancingCaplistTableuForLUA preNoticeFinan = new PreNoticeFinancingCaplistTableuForLUA(StringUtils.isBlank(preNoticeFinanObj.getLimitType()) ? "" : preNoticeFinanObj.getLimitType(),

StringUtils.isBlank(preNoticeFinanObj.getLimitAmountRelation()) ? "" : preNoticeFinanObj.getLimitAmountRelation(),

StringUtils.isBlank(preNoticeFinanObj.getLimitAmountMethod()) ? "" : preNoticeFinanObj.getLimitAmountMethod(),

preNoticeFinanObj.getLookBackDays()!=null ? preNoticeFinanObj.getLookBackDays().toString():"",

StringUtils.isBlank(preNoticeFinanObj.getLookBackDayType()) ? "" : preNoticeFinanObj.getLookBackDayType(),

preNoticeFinanObj.getLimitAmountPercentage()!=null ? preNoticeFinanObj.getLimitAmountPercentage().toString():"",

preNoticeFinanObj.getLimitAmountMaxCap()!=null ? preNoticeFinanObj.getLimitAmountMaxCap().toString() : "",

StringUtils.isBlank(preNoticeFinanObj.getLimitAmountMaxCapCurrency()) ? "" : preNoticeFinanObj.getLimitAmountMaxCapCurrency(),

StringUtils.isBlank(preNoticeFinanObj.getFinancingCapComplex()) ? "" : preNoticeFinanObj.getFinancingCapComplex());

finalPreNoticeList.add(preNoticeFinan);

}

}

if(ma.getLuaPrePostNoticeFinancingCap().isEmpty()){PreNoticeFinancingCaplistTableuForLUA preNoticeFinan = new PreNoticeFinancingCaplistTableuForLUA("","","","","","","","","");

finalPreNoticeList.add(preNoticeFinan);

}

individualAgreementInTableuMessage.getLuakeyterms().setPrenoticefinancingcaplist(finalPreNoticeList);

}

public void setLUAPostNoticeFinanSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<PostNoticeFinancingCaplistTableuForLUA> finalPostNoticeList =new ArrayList<>();

for(LuaPrePostNoticeFinancingCap postNoticeFinanObj : ma.getLuaPrePostNoticeFinancingCap()){

if(null!=postNoticeFinanObj.getPreOrPost() && postNoticeFinanObj.getPreOrPost().equalsIgnoreCase("POST")) {

PostNoticeFinancingCaplistTableuForLUA postNoticeFinan = new PostNoticeFinancingCaplistTableuForLUA(StringUtils.isBlank(postNoticeFinanObj.getLimitType()) ? "" : postNoticeFinanObj.getLimitType(),

StringUtils.isBlank(postNoticeFinanObj.getLimitAmountRelation()) ? "" : postNoticeFinanObj.getLimitAmountRelation(),

StringUtils.isBlank(postNoticeFinanObj.getLimitAmountMethod()) ? "" : postNoticeFinanObj.getLimitAmountMethod(),

postNoticeFinanObj.getLookBackDays() != null ? postNoticeFinanObj.getLookBackDays().toString() : "",

StringUtils.isBlank(postNoticeFinanObj.getLookBackDayType()) ? "" : postNoticeFinanObj.getLookBackDayType(),

postNoticeFinanObj.getLimitAmountPercentage() !=null ? postNoticeFinanObj.getLimitAmountPercentage().toString():"",

postNoticeFinanObj.getLimitAmountMaxCap()!=null ? postNoticeFinanObj.getLimitAmountMaxCap().toString():"",

StringUtils.isBlank(postNoticeFinanObj.getLimitAmountMaxCapCurrency()) ? "" : postNoticeFinanObj.getLimitAmountMaxCapCurrency(),

StringUtils.isBlank(postNoticeFinanObj.getFinancingCapComplex()) ? "" : postNoticeFinanObj.getFinancingCapComplex());

finalPostNoticeList.add(postNoticeFinan);

}

}

if(ma.getLuaPrePostNoticeFinancingCap().isEmpty()){

PostNoticeFinancingCaplistTableuForLUA postNoticeFinan = new PostNoticeFinancingCaplistTableuForLUA("","","","","","","","","");

finalPostNoticeList.add(postNoticeFinan);

}

individualAgreementInTableuMessage.getLuakeyterms().setPostnoticefinancingcaplist(finalPostNoticeList);

}

public void setLUAFishCutBaitSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<FishOrCutBaitTableu> finalFishCutBaitList =new ArrayList<>();

for(FishOrCutBait fishCutBaitObj : ma.getLuaFishOrCutBait()){

FishOrCutBaitTableu fishCutBait = new FishOrCutBaitTableu(StringUtils.isBlank(fishCutBaitObj.getFocbApplicable()) ? "" : fishCutBaitObj.getFocbApplicable(),

StringUtils.isBlank(fishCutBaitObj.getAllEventsOfDefault()) ? "" : fishCutBaitObj.getAllEventsOfDefault(),

StringUtils.isBlank(fishCutBaitObj.getAllTerminationEvents()) ? "" : fishCutBaitObj.getAllTerminationEvents(),fishCutBaitObj.getDeemedWaiverDays() != null ? fishCutBaitObj.getDeemedWaiverDays().toString() : "",

StringUtils.isBlank(fishCutBaitObj.getDeemedWaiverDayType()) ? "" : fishCutBaitObj.getDeemedWaiverDayType());

finalFishCutBaitList.add(fishCutBait);

}

if(ma.getLuaFishOrCutBait().isEmpty()){

FishOrCutBaitTableu fishCutBait = new FishOrCutBaitTableu("","","","","");

finalFishCutBaitList.add(fishCutBait);

}

individualAgreementInTableuMessage.getLuakeyterms().setFishOrCutBaitTableu(finalFishCutBaitList);

}

public void setLUAMarginMaintenanceSection(MasterAgreement ma ,IndividualLUAAgreementInTableuMessage individualAgreementInTableuMessage){

List<MarginMaintenanceTableuForLUA> finalMarginMaintenanceList =new ArrayList<>();

for(MarginMaintenance marginMaintenanceObj : ma.getLuaMarginMaintenance()){

MarginMaintenanceTableuForLUA marginMaintenance = new MarginMaintenanceTableuForLUA(StringUtils.isBlank(marginMaintenanceObj.getMarginNotificationTimePeriod()) ? "" : marginMaintenanceObj.getMarginNotificationTimePeriod(),

StringUtils.isBlank(marginMaintenanceObj.getMarginNotificationTime()) ? "" : marginMaintenanceObj.getMarginNotificationTime(),

StringUtils.isBlank(marginMaintenanceObj.getMarginNotificationTimeLocation()) ? "" : marginMaintenanceObj.getMarginNotificationTimeLocation(),

StringUtils.isBlank(marginMaintenanceObj.getDayType()) ? "" : marginMaintenanceObj.getDayType(),

StringUtils.isBlank(marginMaintenanceObj.getMarginTranferTimePeriod()) ? "" : marginMaintenanceObj.getMarginTranferTimePeriod(),

StringUtils.isBlank(marginMaintenanceObj.getMarginTranferTiming()) ? "" : marginMaintenanceObj.getMarginTranferTiming(),

StringUtils.isBlank(marginMaintenanceObj.getMarginTransferTime()) ? "" : marginMaintenanceObj.getMarginTransferTime(),

StringUtils.isBlank(marginMaintenanceObj.getMarginTransferTimeLocation()) ? "" : marginMaintenanceObj.getMarginTransferTimeLocation(),

StringUtils.isBlank(marginMaintenanceObj.getMarginDelieveryPerPba()) ? "" : marginMaintenanceObj.getMarginDelieveryPerPba());

finalMarginMaintenanceList.add(marginMaintenance);

}

if(ma.getLuaMarginMaintenance().isEmpty()){

MarginMaintenanceTableuForLUA marginMaintenance = new MarginMaintenanceTableuForLUA("","","","","","","","","");

finalMarginMaintenanceList.add(marginMaintenance);

}

individualAgreementInTableuMessage.getLuakeyterms().setMarginmaintenance(finalMarginMaintenanceList);

}

@Override

public void updateEmailForDoNotTrade() {

if (!checkIfJobIsExeOnAnyNode(DO_NOT_TRD_MA_JOB_ID)) {

makeJobExecutionEntry(node, DO_NOT_TRD_MA_JOB_ID, DO_NOT_TRD_MA_JOB_NAME, DO_NOT_TRD_MA_JOB_DESC);

LOGGER.info("Started Job execution for - Mail for Do Not Trade on Master agreements on " + node);

draftStatusUpdateEmail("doNotTradeDate", "DNTR Event", ALL, "dntr_notification.vm", "Do Not Trade on Master agreements - ", "dntrRelations", BeagleDateUtils.addToSystemTime(0, -24, 0, 0));

LOGGER.info("Completed Job execution for - Mail for Do Not Trade on Master agreements on " + node);

}

}

private void draftStatusUpdateEmail(String column, String event, String loc, String vm, String subjectPrefix, String relation, Date dateToCompare) {

DetachedCriteria criteria = DetachedCriteria.forEntityName("CounterPartyRelation");

criteria.add(Restrictions.isNotNull(column));

criteria.add(Restrictions.eq("status", "X"));

criteria.add(Restrictions.ge("statusUpdateDate", dateToCompare));

criteria.addOrder(Order.asc(column));

List<Relation> relations = repository.findObjectsByCriteria(criteria);

List <String> catIds = new ArrayList<>();

relations.stream().forEach((i) -> {

catIds.add(i.getAgreementType().getCategory());

});

if (relations.isEmpty()) {

return;

}



List<String> toList = repository.getFilteredMailIdsForAdminLocAndMailTriggerFromEmailExceptions (loc, event, catIds);

MailService mailService applicationContext.getBean (MailService.class);

String message = SimpleTextMessageBuilder.parseMessage(vm, buildcontext(relation, relations));

String subject = subjectPrefix + DateFormatUtils.format(Calendar.getInstance(), "yyyy/MM/dd");

mailService.sendMimeMail (toList, Collections. <String>emptyList(), subject,message, event, loc, Collections.<String>emptyList());

}



@Override

public void updateNotificationsForSegIA() {

if(!checkIfJobIsExeOnAnyNode (SEG_IA_NOTIFICATION_JOB_ID)) {

makeJobExecutionEntry (node, SEG_IA_NOTIFICATION_JOB_ID,

SEG_IA_NOTIFICATION_JOB_NAME, SEG_IA_NOTIFICATION_JOB_DESC);

LOGGER.info("Started Job execution for Seg IA

Election Notification automatic changes on" node);

DetachedCriteria criteria DetachedCriteria.forEntityName("seg_ia");

criteria.add(Restrictions.isNull("election"));

criteria.add(Restrictions.isNotNull("noticeSentDate"));

criteria.add(Restrictions.eq("noticeSent", true));

criteria.add(Restrictions.le("noticeSentDate", BeagleDateUtils.addXWorking Days Before Today(12)));

List<SegIA> segIAList = repository.findObjectsByCriteria (criteria);
if (segIAList.isEmpty()) {

return;

}

for (SegIA segIA segIAList) {

LOGGER.info("SEG IA Notice Sent MA:" + segIA.getMasterAgreement().getId());

}

MailService mailService applicationContext.getBean (MailService.class);

List<String> toList = repository.getToMailIdListForAdmLocAndMailTrigger (ALL, MailServiceImpl.MAIL_TRIGGER_SEG_IA_NOTIFICATION);
Date noticeSentDate=null;

//for(SegIA segIA segIAList){

if (IsegIAList.isEmpty()) {

String subject ="Seg IA Election Notification

automatic changes";

StringBuilder body new StringBuilder();

body.append("Dear All,\n\n");

body.append("The Seg IA Election has been automatically turned to No on ")

append(DateFormatUtils.format(Calendar.getInstance(), "MM/dd/yyyy")+" for the following:\n\n");

for (SegIA segIA: segIAList) {

noticeSentDate= segIA.getNoticeSentDate();

segIA.setElection (SegIA. Election.NO);

repository.save(segIA);body.append(" MA" + segIA.getMaster Agreement().getId()+" - Entity Name

+ segIA.getMasterAgreement().getCounterpartyEntity().getLeiorCrdsNameForMail() + "

(").append(segIA.getMasterAgreement().getCounterpartyEntity().getCrdsCodesWithLEIForMails()).append(") and all its offices covered.\n\n");

}

body.append("Their Seg IA Notice was sent on ").append(DateFormatUtils.format

(noticeSentDate, "MM/dd/yyyy")).append(".\n\n");

body.append("Regards, \nBeagle Support");

mailService.sendSegIANotificationMail (tolist, Collections.<String>emptyList(),

}

subject, body.toString(), Collections.<String>emptyList(), ALL);

LOGGER.info("Completed Job execution for - Seg IA

Election Notification automatic changes on "+ node);
}
}
private Map<String, Object> buildcontext(String relation, List<Relation> relations) {

Map<String, Object> map = new HashMap<String, Object>();

map.put(relation, relations);

return map;

}

@Override

public void autoLinkExecutionAgreements (MasterAgreement masterAgreement) {

final boolean isCDEAEuropean ExecutionAgreement= AgreementType.CDEA_EUROPEAN_EXECUTION_AGREEMENT. equalsIgnoreCase (masterAgreement.getAgreementType().getId().getId());

final boolean isDRVGermanExecutionAgreement= AgreementType.DRV_GERMAN_EXECUTION_AGREEMENT. equalsIgnoreCase (masterAgreement.getAgreementType().getId().getId());

final boolean isFBFF FrenchExecutionAgreement= AgreementType.FBF_FRENCH_EXECUTION_AGREEMENT. equalsIgnoreCase (masterAgreement.getAgreementType().getId().getId());

final boolean isEuropeanExecutionAgreement= AgreementType.EUROPEAN_EXECUTION_AGREEMENT. equalsIgnoreCase (masterAgreement.getAgreementType().getId().getId());


final boolean isEuroMasterExecutionAgreement = AgreementType.EUROMASTER_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId());

final boolean isCDEAFrenchExecutionAgreement=AgreementType.CDEA_FRENCH_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId());

String identifier = "";

if (isDRVGermanExecutionAgreement) {

identifier = "DRV";

} else if (isCDEAEuropeanExecutionAgreement || isCDEAFrenchExecutionAgreement) {

identifier = "CDEA";

} else if (isEuropeanExecutionAgreement) {

identifier = "EUEA";

} else if (isFBFFrenchExecutionAgreement) {

identifier = "FBFFR";

} else if (isEuroMasterExecutionAgreement) {

identifier = "EUROMASTER";

}

if (!("".equals(identifier))) {

List<NonClearingAgreementTypesLookup> agreementTypes = repository.getNonClearingBilAgrList(identifier);

List<AgreementTypeKey> agreementTypeKeys = new ArrayList<AgreementTypeKey>();

for (int i = 0; i < agreementTypes.size(); i++) {

agreementTypeKeys.add(agreementTypes.get(i).getId());

}

List<Long> pairedEntities = repository.getPairedEntities(masterAgreement.getBnpParibasEntity().getId());

List agreements = repository.getMasterAgreementsBetweenBNPAndCP(masterAgreement.getBnpParibasEntity().getId(), masterAgreement.getCounterpartyEntity().getId(), agreementTypeKeys);

for (Long pairedEntity : pairedEntities) {

agreements.addAll(repository.getMasterAgreementsBetweenBNPAndCP(pairedEntity, masterAgreement.getCounterpartyEntity().getId(), agreementTypeKeys));

}

List executedMas = new ArrayList();

for (int i = 0; i < agreements.size(); i++) {

if (((MasterAgreement) agreements.get(i)).isMAMasterAMDExecuted()) {executedMas.add((MasterAgreement) agreements.get(i));

}

}

Set<NonClearingBilateralMasterAgreement> nonClearingBilateralMasterAgreements = new HashSet<NonClearingBilateralMasterAgreement>();

if (executedMas.isEmpty()) {

NonClearingBilateralMasterAgreement clearingBilateralMasterAgreement = new NonClearingBilateralMasterAgreement();

clearingBilateralMasterAgreement.setMasterAgreement(masterAgreement);

clearingBilateralMasterAgreement.setMatyp_id(NonClearingBilateralMasterAgreement.STAND_ALONE_AGR_TYPE);

clearingBilateralMasterAgreement.setMatypYear(2014L);

clearingBilateralMasterAgreement.setMatypMonth(1L);

clearingBilateralMasterAgreement.setNonClearingBilateralMANumber(0L);

nonClearingBilateralMasterAgreements.add(clearingBilateralMasterAgreement);

} else if (executedMas.size() == 1) {

NonClearingBilateralMasterAgreement clearingBilateralMasterAgreement = new NonClearingBilateralMasterAgreement();

clearingBilateralMasterAgreement.setMasterAgreement(masterAgreement);clearingBilateralMasterAgreement.setMatyp_id(((MasterAgreement) (executedMas.get(0))).getAgreementType().getId().getId());

clearingBilateralMasterAgreement.setMatypYear(((MasterAgreement) (executedMas.get(0))).getAgreementType().getId().getYear());

clearingBilateralMasterAgreement.setMatypMonth(((MasterAgreement) (executedMas.get(0))).getAgreementType().getId().getMonth());

clearingBilateralMasterAgreement.setNonClearingBilateralMANumber(((MasterAgreement) (executedMas.get(0))).getId());

nonClearingBilateralMasterAgreements.add(clearingBilateralMasterAgreement);

}

masterAgreement.setNonClearingBilateralMasterAgreements(nonClearingBilateralMasterAgreements);

}

}

@Override

public MasterAgreement createCopyAndSaveMasterAgreement(AgreementType agreementType, EntityIds counterparty, EntityIds bnpParibas,

String negotiator, Location adminLocation, Long governingLaw,

String domesticBranch, Date fileOpenedDate, String agreementWithCSA,

boolean createDefaultOffices, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, Long legalCounterpartyType, Long maosRequestId, List clearingHouses, CollateralTypeLookup collateralType, Date collNegoStartDate, User user, Integer sourceMaId, LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract, LookupItem partyPostingCollateral, Boolean creditQuestionnaireReceived, Date creditQuestionnaireReceivedDate, Boolean isAutoCreateCdea) throws InvalidMasterAgreementException {

MasterAgreement targetMA = previewMasterAgreementCopy(agreementType, counterparty, bnpParibas, negotiator,

adminLocation, governingLaw, domesticBranch, fileOpenedDate, agreementWithCSA, createDefaultOffices,

legalCounterpartyType, fortisFlag, closeOutNettingFlag, transactionSpecificMasters, true, maosRequestId, collateralType, collNegoStartDate, sourceMaId, regulatoryClassification, collateralMarginType,collateralRegime, StandardisedContract, isAutoCreateCdea);

String action = "";

String agreementTypeId = targetMA.getAgreementType().getId().getId();

if (isClearingHouseAgreementType(agreementTypeId)) {

setClearingHouses(clearingHouses, targetMA);

}

if (targetMA.getAgreementType().isDerivatives() && ((GoverningLaw) targetMA.getGoverningLaw()).isFrench() && LookupItem.getBnppEntityIdList().contains(targetMA.getBnpParibasEntity().getId())) {

AgreementJoin agreementJoin = AgreementHelper.getSpecialClauseInstance(targetMA, LookupItem.SPC_NEW_CTD_CODE, true);

targetMA.getSpecialClauseJoins().remove(agreementJoin);

targetMA.updateNegotiationDateForMa();

agreementJoin.setModification(User.BEAGLE, new Date());

targetMA.addSpecialClause(agreementJoin);

}

targetMA.setNegoStatusCommentLastModifiedDate(new Date());

targetMA.setNegotiationDate(new Date());

createAndSaveCreditQuestionnaireDetailsForNegotiation(targetMA, creditQuestionnaireReceived, creditQuestionnaireReceivedDate);

AgreementHelper.copyContactAndCorrespondenceTab(Long.valueOf(sourceMaId), targetMA, repository);

LOGGER.info("Master Agreement Created with Id : " + targetMA.getId());

if(((targetMA.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(targetMA)) ||

targetMA.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(targetMA.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(targetMA.getGoverningLaw()).toString()) == 6 : false) {

targetMA.setCounterpartyAutomaticTermination(true);

targetMA.setBnpParibasAutomaticTermination(true);

}

repository.save(targetMA);

repository.getSession().flush();

action = "CreateMasterAgreement";

Entity entity = repository.findObject(Entity.class, counterparty.entity);

regulationsAndProtocolsService.assignIsdaRspAndIsdaJmpProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(),repository);

regulationsAndProtocolsService.assignArt55BailInProtocolToMa(

entity.getLegalEntityIdentifier() != null ? Arrays.asList(entity.getLegalEntityIdentifier().getLeiId())

: new ArrayList<>(),repository);

regulationsAndProtocolsService.updateBMRProtoclForMA(targetMA,entity.getLegalEntityIdentifier() != null ?

entity.getLegalEntityIdentifier().getLeiId() : null);

regulationsAndProtocolsService.copySFTRDataForMa(Long.valueOf(sourceMaId),targetMA);

regulationsAndProtocolsService.copyRSASftrDataForMa(Long.valueOf(sourceMaId),targetMA);

regulationsAndProtocolsService.copyRSAEmirDataForMa(Long.valueOf(sourceMaId),targetMA);

setSftrFlagForCreateMa(targetMA);

copyPBAKeyTermsDataFromSourceMaToTargetMa(targetMA,Long.valueOf(sourceMaId));

copyLUAKeyTermsDataFromSourceMaToTargetMa(targetMA,Long.valueOf(sourceMaId));

return targetMA;

}

private MasterAgreement previewMasterAgreementCopy(AgreementType agreementType,

EntityIds counterparty, EntityIds bnpParibas,

String negotiator,

Location adminLocation, Long governingLaw, String domesticBranch,

Date fileOpenedDate,

String agreementWithCSA,

boolean createDefaultOffices, Long legalCounterpartyType, Boolean fortisFlag, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, boolean doSave, Long maosRequestId, CollateralTypeLookup collateralType, Date collNegoStartDate, Integer sourceMaId, LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract, Boolean isAutoCreateCdea) throws InvalidMasterAgreementException {

MasterAgreement sourceMa = (MasterAgreement) repository.getSession().get(MasterAgreement.class, Long.valueOf(sourceMaId));

MasterAgreement targetMa = new MasterAgreement();

targetMa.setSourceMAId(new Long(sourceMaId));

targetMa.setAgreementType(agreementType);

targetMa.setGoverningLaw(repository.findGoverningLaw(governingLaw));

targetMa.setPdg(Boolean.FALSE);

targetMa.setCloseOutNetting(closeOutNettingFlag);

targetMa.setFortisFlag(fortisFlag);

targetMa.setTransactionSpecificMasters(transactionSpecificMasters);

setCounterpartyEntities(targetMa, counterparty);

setBnpParibasEntities(targetMa, bnpParibas);

targetMa.setFileOpenedDate(fileOpenedDate);

targetMa.setWithCSA(AmendmentType.hasCSA(agreementWithCSA));

if(AgreementCategoryHelper.isPBCategory(targetMa.getAgreementType().getCategory()))

targetMa.setLegalCounterpartyType(sourceMa.getLegalCounterpartyType());

Amendment amendment = createAndAddFirstAmendmentForCopyMa(targetMa, adminLocation, negotiator, domesticBranch, agreementWithCSA,

doSave, maosRequestId, closeOutNettingFlag, transactionSpecificMasters, collateralType, collNegoStartDate, regulatoryClassification, collateralMarginType, collateralRegime, StandardisedContract);

amendment.setComment(sourceMa.getLastAmendment().getComment());

if (createDefaultOffices) {

AgreementHelper.createDefaultOffices(targetMa, repository);

}

//Offices Covered

Long selectedBnpEntityId = bnpParibas.entity;

Long sourceMaBnpEntityId = sourceMa.getBnpParibasEntity().getId();

/*If source ma bnp entity is selected*/

if (selectedBnpEntityId.compareTo(sourceMaBnpEntityId) == 0) {

AgreementHelper.cloneBnpParibasCoveredOffices(targetMa, sourceMa.getCoveredOffices(Party.BNP_PARIBAS));

}

//Products covered

AgreementHelper.cloneCoveredProducts(targetMa, sourceMa.getCoveredProductJoins());

if (sourceMa.getAgreementType().isIsdaAgreement() && !agreementType.isLinkedCdeaAgreementType()) {

AgreementHelper.cloneCoveredProductsAuditDetails(targetMa, sourceMa);

}

targetMa.updateCoveredProductIntegrity();

AgreementHelper.cloneProductNetting(targetMa.getCoveredProductJoins(), sourceMa.getCoveredProductJoins());

//Legal data

AgreementHelper.propagateLegalDataForCopyMa(targetMa, sourceMa);

//Cross default

AgreementHelper.cloneCrossDefaultDetails(targetMa, sourceMa);

//Regulatory fatca details

Set<FatcaProtocolJoin> sourceMaFatcaDetails = sourceMa.getFatcaProtocolJoins();

Set<FatcaProtocolJoin> targetMaFatcaDetails = new HashSet<FatcaProtocolJoin>();

for (FatcaProtocolJoin fatcaProtocolJoin : sourceMaFatcaDetails) {

FatcaProtocolJoin fatcaProtocolJoin1 = new FatcaProtocolJoin(targetMa, fatcaProtocolJoin.getId().getJoinedId());

fatcaProtocolJoin1.setValue(fatcaProtocolJoin.getValue());

targetMaFatcaDetails.add(fatcaProtocolJoin1);

}

targetMa.setFatcaProtocolJoins(targetMaFatcaDetails);

//Hire act details

MasterAgreement maFromWhereToCopyData = repository.findMaster(new Long(sourceMaId)).getMasterAgreement();

Set<HireProtocolJoin> sourceMaHireActDetails = maFromWhereToCopyData.getHireProtocolJoins();

Set<HireProtocolJoin> targetMaHireActDetails = new HashSet<HireProtocolJoin>();

for (HireProtocolJoin hireActProtocolJoin : sourceMaHireActDetails) {

HireProtocolJoin hireActProtocolJoin1 = new HireProtocolJoin(targetMa, hireActProtocolJoin.getId().getJoinedId());

hireActProtocolJoin1.setValue(hireActProtocolJoin.getValue());

targetMaHireActDetails.add(hireActProtocolJoin1);

}

targetMa.setHireProtocolJoins(targetMaHireActDetails);

//BMR details

Set<IsdaBmrData> sourceMAIsdaBmrData = maFromWhereToCopyData.getIsdaBmrData();

Set<IsdaBmrData> targetMaIsdaBmrData = new HashSet<IsdaBmrData>();

for (IsdaBmrData isdaBmrData : sourceMAIsdaBmrData) {

IsdaBmrData brrdData1 = new IsdaBmrData(targetMa, isdaBmrData.getIdentifier(), isdaBmrData.getValue(),isdaBmrData.getDateOfAdherence(), isdaBmrData.getRevokeDate());

brrdData1.setValue(isdaBmrData.getValue());

targetMaIsdaBmrData.add(brrdData1);

}

targetMa.setIsdaBmrData(targetMaIsdaBmrData);

//BRRD details

Set<BrrdData> sourceMABrrdData = maFromWhereToCopyData.getBrrdData();

Set<BrrdData> targetMABrrdData = new HashSet<BrrdData>();

for(BrrdData brrdData: sourceMABrrdData){

BrrdData brrdData1 = new BrrdData(targetMa, brrdData.getIdentifier(), brrdData.getValue(),brrdData.getClauseDate(), brrdData.getRevokeDate());

brrdData1.setValue(brrdData.getValue());

targetMABrrdData.add(brrdData1);

}targetMa.setBrrdData(targetMABrrdData);

//Art71 BRRD Data

Set<Art71BrrdData> sourceMAArt71 = maFromWhereToCopyData.getArt71brrdData();

Set<Art71BrrdData> targetMAArt71 = new HashSet<Art71BrrdData>();

for(Art71BrrdData art71: sourceMAArt71){

Art71BrrdData art711 = new Art71BrrdData(targetMa, art71.getIdentifier(), art71.getValue(),art71.getDateOfAdherence(), art71.getRevokeDate());

art711.setValue(art71.getValue());

targetMAArt71.add(art711);

}

targetMa.setArt71brrdData(targetMAArt71);

//PR DR EMIR Data

if(null != sourceMaId && AgreementHelper.isAgreementTypEligibleForPRDRMandatoryCheck(targetMa.getAgreementType()) && !targetMa.getAgreementType().isNoMasterPRDRAgreement()) {

targetMa.setEmirSpecialClauseJoins(copyEMIRPRDRClauseFormSourceMa(maFromWhereToCopyData.getEmirSpecialClauseJoins(), targetMa, maFromWhereToCopyData.getAgreementDate()));

}//NO MASTER AGREEMENT COPY

else if(null!= targetMa.getAgreementType() && targetMa.getAgreementType().isNoMasterPRDRAgreement()) {

Set<EMIRSpecialClauseJoin> targetEmirSpecialClauseJoins = new HashSet<>();

targetEmirSpecialClauseJoins.add(copyEMIRPRDRClauseForNMPRDRAgr(targetMa,null));

targetMa.setEmirSpecialClauseJoins(targetEmirSpecialClauseJoins);

}

if (maosRequestId != null) {

MaosDetails maosDetails = repository.getObject(MaosDetails.class, maosRequestId);

if (maosDetails != null) {

MaosIdJoin maosIdJoin = new MaosIdJoin(targetMa, maosDetails);

targetMa.setLinkedMaosJoin(maosIdJoin);

maosDetails.getMaosLinkedMAs().add(maosIdJoin);

}

}

targetMa.validateEntities();

targetMa.setInitialDefaultsCopyMa();

NettingCalculator calculator = new NettingCalculator(targetMa, LegalOpinion.getLegalOpinion(repository, targetMa));

calculator.calculateCoveredProductNetting(true);

if(isAutoCreateCdea)

isdaCdeaLinkageControlService.removeFieldsForCdea(targetMa);

//autoLinkExecutionAgreements(targetMa);

return targetMa;

}

private Set<CollateralRegimeDetail> createCollateralRegimeDetail(CollateralData collateralData, Set<LookupItem> collateralRegime) {Set<CollateralRegimeDetail> collateralRegimeDetailSet = new HashSet<CollateralRegimeDetail>();

if (collateralRegime != null) {

for (Iterator<LookupItem> collateralRegimeIterator = collateralRegime.iterator(); collateralRegimeIterator.hasNext(); ) {

LookupItem collateralRegimeObj = collateralRegimeIterator.next();

CollateralRegimeDetail collateralRegimeDetail = new CollateralRegimeDetail();

collateralRegimeDetail.setCollateralData(collateralData);

collateralRegimeDetail.setCollateralRegime(collateralRegimeObj);

collateralRegimeDetailSet.add(collateralRegimeDetail);

}

}

return collateralRegimeDetailSet;

}

private CollateralHeaderDetails getCollateralHeaderDetails(LookupItem regulatoryClassification, LookupItem collateralMarginType, Long collateralId) {

if (regulatoryClassification.getId().equals(Long.valueOf(3))) {

return repository.getCollateralHeaderDetails(Long.valueOf(1));

} else if (regulatoryClassification.getId().equals(Long.valueOf(1)) && collateralMarginType.getId().equals(Long.valueOf(1))) {return repository.getCollateralHeaderDetails(Long.valueOf(2));

}  else if (regulatoryClassification.getId().equals(Long.valueOf(1)) && collateralMarginType.getId().equals(Long.valueOf(2))) {

return repository.getCollateralHeaderDetails(Long.valueOf(4));

} else if (regulatoryClassification.getId().equals(Long.valueOf(2))) {

return repository.getCollateralHeaderDetails(Long.valueOf(5));

}

return null;

}

private String generateCollateralSegmentId(CollateralData collateralData, CollateralHeaderDetails collateralHeaderDetails) {

if (collateralData.getCollateralId() != null) {

return collateralData.getCollateralId() + "_" + collateralHeaderDetails.getCollateralNumberSuffix();

}

return null;

}

@Override

public Map<String, String> getTabAuditPermissions() {

return tabAuditPermissions;

}

@Override

@SuppressWarnings("squid:S1067")public MasterAgreement createNewAgreementInGroup(Long maIdFromWhereToCopyData, EntityIds entityIds, Date dateAddedToGroup, boolean removeSignedDate, List<PartyMaosDetails> maosDetails, Long[] amendmentIdFromWhereToCopyData, boolean removeCollatSignDate, String departmentPartyNo, Boolean isLinkedMasterAgreeement, AgreementType masterAgreementType) {

MasterAgreement maFromWhereToCopyData = MasterAgreement.get(repository, maIdFromWhereToCopyData);

boolean counterpartyManaged = maFromWhereToCopyData.getActingEntity(Party.COUNTERPARTY) != null;

amendmentIdFromWhereToCopyData = new Long[1];

if (maFromWhereToCopyData.getLastAmendment() != null)

amendmentIdFromWhereToCopyData[0] = maFromWhereToCopyData.getLastAmendment().getAmendmentId();

if (amendmentIdFromWhereToCopyData == null) {

amendmentIdFromWhereToCopyData[0] = 0L;

}

MasterAgreement masterAgreement = new MasterAgreement();

Entity counterparty;

Entity bnpParibas;

if (counterpartyManaged) {

counterparty = retrieveEntity(entityIds.entity);

bnpParibas = maFromWhereToCopyData.getSigningEntity(Party.BNP_PARIBAS);

} else {

counterparty = maFromWhereToCopyData.getSigningEntity(Party.COUNTERPARTY);

bnpParibas = retrieveEntity(entityIds.entity);

}

Entity cpActingEntity= isdaCdeaLinkageControlService.

getCpActingEntity(maFromWhereToCopyData.getActingEntity(Party.COUNTERPARTY),maFromWhereToCopyData.getBnpParibasEntity(),isLinkedMasterAgreeement);

masterAgreement.setCounterpartyEntities(counterparty,

cpActingEntity,

maFromWhereToCopyData.getFundManagingCompany(Party.COUNTERPARTY),

maFromWhereToCopyData.getFundWithCompartments(Party.COUNTERPARTY));

masterAgreement.setBnpParibasEntities(bnpParibas,

maFromWhereToCopyData.getActingEntity(Party.BNP_PARIBAS),

maFromWhereToCopyData.getFundManagingCompany(Party.BNP_PARIBAS),

maFromWhereToCopyData.getFundWithCompartments(Party.BNP_PARIBAS));

if (isLinkedMasterAgreeement) {

List<Amendment> amd = new ArrayList<Amendment>();

amd.add(maFromWhereToCopyData.getMaster());

masterAgreement.duplicateForAgreement(maFromWhereToCopyData, removeSignedDate, removeCollatSignDate, amd, repository, isLinkedMasterAgreeement, masterAgreementType, true,false,null,null,null, false, auditor.getUser());

masterAgreement.setGoverningLaw(isdaCdeaLinkageControlService.getGoverningLawNY());

isdaCdeaLinkageControlService.removeFieldsForCdea(masterAgreement);

} else {

if (removeSignedDate) {

List<Amendment> amd = new ArrayList<Amendment>();

amd.add(maFromWhereToCopyData.getMaster());

masterAgreement.duplicateForAgreement(maFromWhereToCopyData, removeSignedDate, removeCollatSignDate, amd, repository, isLinkedMasterAgreeement, null, true,false,null,null,null, false, auditor.getUser());

} else {

masterAgreement.duplicateForAgreement(maFromWhereToCopyData, removeSignedDate, removeCollatSignDate, maFromWhereToCopyData.getAmendments(), repository, isLinkedMasterAgreeement, null, true,false,null,null,null, false, auditor.getUser());

}

}

Set<HireProtocolJoin> sourceMaHireActDetails = maFromWhereToCopyData.getHireProtocolJoins();

Set<HireProtocolJoin> targetMaHireActDetails = new HashSet<HireProtocolJoin>();

for (HireProtocolJoin hireActProtocolJoin : sourceMaHireActDetails) {

HireProtocolJoin hireActProtocolJoin1 = new HireProtocolJoin(masterAgreement, hireActProtocolJoin.getId().getJoinedId());

hireActProtocolJoin1.setValue(hireActProtocolJoin.getValue());

targetMaHireActDetails.add(hireActProtocolJoin1);

}

masterAgreement.setHireProtocolJoins(targetMaHireActDetails);

Set<FatcaProtocolJoin> sourceMaFatcaDetails = maFromWhereToCopyData.getFatcaProtocolJoins();

Set<FatcaProtocolJoin> targetMaFatcaDetails = new HashSet<FatcaProtocolJoin>();

for (FatcaProtocolJoin fatcaProtocolJoin : sourceMaFatcaDetails) {

FatcaProtocolJoin fatcaProtocolJoin1 = new FatcaProtocolJoin(masterAgreement, fatcaProtocolJoin.getId().getJoinedId());

fatcaProtocolJoin1.setValue(fatcaProtocolJoin.getValue());

targetMaFatcaDetails.add(fatcaProtocolJoin1);

}masterAgreement.setFatcaProtocolJoins(targetMaFatcaDetails);

Set<IsdaBmrData> sourceMAIsdaBmrData = maFromWhereToCopyData.getIsdaBmrData();

Set<IsdaBmrData> targetMaIsdaBmrData = new HashSet<IsdaBmrData>();

for (IsdaBmrData isdaBmrData : sourceMAIsdaBmrData) {

IsdaBmrData brrdData1 = new IsdaBmrData(masterAgreement, isdaBmrData.getIdentifier(), isdaBmrData.getValue(),isdaBmrData.getDateOfAdherence(), isdaBmrData.getRevokeDate());

brrdData1.setValue(isdaBmrData.getValue());

targetMaIsdaBmrData.add(brrdData1);

}

masterAgreement.setIsdaBmrData(targetMaIsdaBmrData);

Set<BrrdData> sourceMABrrdData = maFromWhereToCopyData.getBrrdData();

Set<BrrdData> targetMABrrdData = new HashSet<BrrdData>();

for(BrrdData brrdData: sourceMABrrdData){

BrrdData brrdData1 = new BrrdData(masterAgreement, brrdData.getIdentifier(), brrdData.getValue(),brrdData.getClauseDate(), brrdData.getRevokeDate());

brrdData1.setValue(brrdData.getValue());

targetMABrrdData.add(brrdData1);

}

masterAgreement.setBrrdData(targetMABrrdData);

Set<Art71BrrdData> sourceMAArt71 = maFromWhereToCopyData.getArt71brrdData();

Set<Art71BrrdData> targetMAArt71 = new HashSet<Art71BrrdData>();

for(Art71BrrdData art71: sourceMAArt71){

Art71BrrdData art711 = new Art71BrrdData(masterAgreement, art71.getIdentifier(), art71.getValue(),art71.getDateOfAdherence(), art71.getRevokeDate());

art711.setValue(art71.getValue());

targetMAArt71.add(art711);

}

masterAgreement.setArt71brrdData(targetMAArt71);

if(AgreementHelper.isAgreementTypEligibleForPRDRMandatoryCheck(masterAgreement.getAgreementType())) {

masterAgreement.setEmirSpecialClauseJoins(copyEMIRPRDRClauseFormSourceMa(maFromWhereToCopyData.getEmirSpecialClauseJoins(), masterAgreement, maFromWhereToCopyData.getAgreementDate()));

}

if (isLinkedMasterAgreeement) {

masterAgreement.setAddToGroupDate(dateAddedToGroup);

}

masterAgreement.setCounterpartyIsLegal(masterAgreement.getSigningEntity(Party.COUNTERPARTY).isLegal());masterAgreement.setBnpParibasIsLegal(masterAgreement.getSigningEntity(Party.BNP_PARIBAS).isLegal());

masterAgreement.setSourceMAId(maIdFromWhereToCopyData);

copyNegotiationDetails(masterAgreement, maFromWhereToCopyData);

/*Amendment amendment = masterAgreement.getMaster();

if (!removeSignedDate && (counterpartyManaged && maFromWhereToCopyData.isExecuted() && counterparty.isPending() && counterparty.isActive()) == false) {

amendment.execute(auditor.getUser(), maFromWhereToCopyData.getMaster().getSignedDate());

}*/

//changed after maosid moved to MA.. cautious

PartyMaosDetails maos = PartyMaosDetails.getPartyMaosDetailsByParty(maosDetails, counterparty.getId());

MaosIdJoin maosIdJoinObj=null;

MaosDetails maosDetail=null;

if (maos != null) {

maosDetail = repository.getObject(MaosDetails.class, maos.getMaosId());

if (maosDetail != null) {

maosIdJoinObj= new MaosIdJoin(masterAgreement, maosDetail);

UpdateBeagleOnMaosFeed.updateAnAgreement(maosDetail, masterAgreement, repository);

}

}if (masterAgreement.isBnpLondonOfficesRequired()) {

masterAgreement.addBnpParibasCoveredOffice(AgreementHelper.createCoveredOffice(masterAgreement, Entity.find(repository, Entity.BNPL_LON), Party.BNP_PARIBAS));

masterAgreement.addBnpParibasCoveredOffice(AgreementHelper.createCoveredOffice(masterAgreement, Entity.find(repository, Entity.PARB_LON), Party.BNP_PARIBAS));

}

if (masterAgreement.getAgreementType().isDerivatives() && ((GoverningLaw) masterAgreement.getGoverningLaw()).isFrench() && LookupItem.getBnppEntityIdList().contains(masterAgreement.getBnpParibasEntity().getId())) {

AgreementJoin agreementJoin = AgreementHelper.getSpecialClauseInstance(masterAgreement, LookupItem.SPC_NEW_CTD_CODE, true);

masterAgreement.getSpecialClauseJoins().remove(agreementJoin);

agreementJoin.setModification(User.BEAGLE, new Date());

masterAgreement.addSpecialClause(agreementJoin);

}

masterAgreement.setNegoStatusCommentLastModifiedDate(new Date());

masterAgreement.updateNegotiationDateForMa();regulationsAndProtocolsService.copyRSASftrDataForMa(Long.valueOf(maIdFromWhereToCopyData),masterAgreement);

if(((masterAgreement.getAgreementType().toString().equals("RAHMENVERT (2018)") && !AgreementHelper.isRahmenvertEligibleForPrefilling(masterAgreement)) ||

masterAgreement.getAgreementType().toString().equals("Clearing-Rahmenvereinbarung Ger CRV (2019)")) &&

(masterAgreement.getGoverningLaw()!=null) ? new Long(LookupItem.idOf(masterAgreement.getGoverningLaw()).toString()) == 6 : false) {

masterAgreement.setCounterpartyAutomaticTermination(true);

masterAgreement.setBnpParibasAutomaticTermination(true);

}

if (masterAgreement.getId() != null) {

repository.getSession().merge(masterAgreement);

} else {

repository.save(masterAgreement);

}

if(maosIdJoinObj!=null){

masterAgreement.setLinkedMaosJoin(maosIdJoinObj);

maosDetail.getMaosLinkedMAs().add(maosIdJoinObj);

}

/*try {

indexingService.reindexElement(masterAgreement, masterAgreement.getId(), EMIR.class);

} catch (IndexNotFound indexNotFound) {

LOGGER.error("EMIR Indexing not found", indexNotFound);

}*/

autoLinkExecutionAgreements(masterAgreement);

if (counterpartyManaged && maFromWhereToCopyData.isExecuted() && !removeSignedDate) {

for (Iterator<MaosIdJoin> maosIdJoinIterator = masterAgreement.getLinkedMaosJoin().iterator(); maosIdJoinIterator.hasNext(); ) {

MaosIdJoin maosIdJoin = maosIdJoinIterator.next();

if (maosIdJoin.getMaosDetails() != null) {

if (maosIdJoin.getMaosDetails().getCountOfMAsInNego(masterAgreement.getId()) == 0 && maosIdJoin.getMaosDetails().getCountOfMAsInPreExecuted(masterAgreement.getId()) == 0) {

if (maosIdJoin.getMaosDetails().getMaosIdStatus() == null) {

maosIdJoin.getMaosDetails().setMaosIdStatus(MaosDetails.MAOS_ID_STATUS_APPROVED);

clientOnboardingService.notifyMaos(maosIdJoin.getMaosDetails(), MaosNotification.APPROVED);

}

}

}

}

}

if (AgreementType.EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId()) || AgreementType.EUROPEAN_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId()) || AgreementType.CDEA_EUROPEAN_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId())

|| AgreementType.DRV_GERMAN_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId()) || AgreementType.FBF_FRENCH_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId())

|| AgreementType.EUROMASTER_EXECUTION_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId())) {

if (!maFromWhereToCopyData.getClearingHouses().isEmpty()) {

masterAgreement.getClearingHouses().add(new ClearingHouseValue(masterAgreement, (Long) maFromWhereToCopyData.getClearingHouses().iterator().next().getId().getJoinedId()));

}

}if (!isLinkedMasterAgreeement && AgreementType.CUSTOMER_ACCOUNT_AGREEMENT.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId())

&& masterAgreement.getLastAmendment().getAdminLocation().isNewYork()) {

for (Long clearingHouseValue : repository.getFcmClearingHouseList()) {

masterAgreement.getClearingHouses().add(new ClearingHouseValue(masterAgreement, clearingHouseValue));

}

}

if (null != departmentPartyNo) {

masterAgreement.addCounterpartyCoveredOffice(AgreementHelper.createCoveredOffice(masterAgreement, Entity.find(repository, Long.parseLong(departmentPartyNo)), Party.COUNTERPARTY));

}

createAndSaveCreditQuestionnaireDetailsForNegotiation(masterAgreement, null, null);

repository.getSession().flush();

setSftrFlagForCreateMa(masterAgreement);

return masterAgreement;

}

private Amendment createAndAddFirstAmendmentForCopyMa(MasterAgreement targetMa, Location location, String negotiator, String domesticBranch,

String agreementWithCSA, boolean doSave, Long maosRequestId, Boolean closeOutNettingFlag, Boolean transactionSpecificMasters, CollateralTypeLookup collateralType, Date collNegoStartDate,

LookupItem regulatoryClassification,

LookupItem collateralMarginType,

Set<LookupItem> collateralRegime, Boolean StandardisedContract) {

Amendment amendment = new Amendment();

Negotiator negotiatorLookup = Negotiator.find(repository, negotiator);

amendment.setAdminLocation(location);

amendment.setNegotiator(negotiatorLookup);

if (domesticBranch != null  &&  !"".equals(domesticBranch)) {

amendment.setDomesticBranch(repository.getObject(DomesticBranch.class, domesticBranch));

}

if (StandardisedContract != null) {

amendment.setStandardisedContract(StandardisedContract);

}

if (targetMa.getAgreementType().isDerivatives()) {

if (AmendmentType.MA_ONLY.equals(agreementWithCSA) || (!targetMa.belongsToGroup() && AgreementType.getClearingTypes().contains(targetMa.getAgreementType().getId().getId()))) {

amendment.setType(AmendmentType.get(repository, AmendmentType.AMENDMENT_TO_MA_ONLY_ID));

amendment.setCollateralStatus(CollateralStatus.get(repository, CollateralStatus.NO_CSA_ID));

}

if (AmendmentType.MA_AND_CSA.equals(agreementWithCSA)) {

amendment.setType(AmendmentType.get(repository, AmendmentType.AMENDMENT_TO_MA_AND_COLLATERAL_ID));

targetMa.setCollateralFlag(true);

}

}

targetMa.addAmendment(amendment);

AmendmentKey key = new AmendmentKey(targetMa, Long.valueOf(0));

amendment.setId(key);

AgreementType agreementType = targetMa.getAgreementType();

targetMa.setCounterpartyCrossDefault(new CrossDefault(targetMa));

targetMa.setBnpParibasCrossDefault(new CrossDefault(targetMa));

if (agreementType.isDerivatives()) {

if (amendment.getType() != null && !AmendmentType.AMENDMENT_TO_MA_ONLY_ID.equals(amendment.getType().getId())) {

targetMa.setCollateralData(new CollateralData(targetMa));

}

} else if (agreementType.isStockLending()) {

targetMa.setStockLending(new StockLending(targetMa));

} else if (agreementType.isRepurchase()) {

targetMa.setRepurchase(new Repurchase(targetMa));

}

if (collateralType != null) {

CollateralData collateralData = targetMa.getCollateralData();

if (collateralData != null) {

collateralData.setCollateralId(repository.fetchNextCollateralSeq());

CollateralHeaderDetails collateralHeaderDetails = getCollateralHeaderDetails(regulatoryClassification, collateralMarginType, collateralData.getCollateralId());

collateralData.setType(collateralType);

collateralData.setAdminLocation(location);

collateralData.setNegotiator(negotiatorLookup);

collateralData.setNegotiationStartDate(collNegoStartDate);

collateralData.setCsaLawId(collateralType.getCollateralTypeLaw() != null ? collateralType.getCollateralTypeLaw().getId().toString() : null);

collateralData.setRegulatoryClassification(regulatoryClassification);

collateralData.setCollateralMarginType(collateralMarginType);

collateralData.setCollateralRegimeDetail(createCollateralRegimeDetail(collateralData, collateralRegime));collateralData.setCollateralSegmentId(generateCollateralSegmentId(collateralData, collateralHeaderDetails));

collateralData.setCollateralHeaderDetails(collateralHeaderDetails);

}

}

return amendment;

}

/* BGL-1321 - Negotiation details to be copied in case of copyforall MA selected */

private void copyNegotiationDetails(MasterAgreement masterAgreement, MasterAgreement maFromWhereToCopyData) {

Negotiation negoDetails;

if (null != masterAgreement.getGroup()) {

negoDetails = masterAgreement.getGroup().getNegotiation();

} else {

negoDetails = maFromWhereToCopyData.getNegotiation();

}

if (negoDetails != null) {

Negotiation negotiation = repository.findOrCreateNegotiationForAgreement(masterAgreement);

BeagleUtils.copyProperties(negoDetails, negotiation, ListBuilder.build(BeagleUtils.getNegotiationAllProperties()));

masterAgreement.getLastAmendment().forceFlush();

}

}

public BeagleService getBeagleService() {

return beagleService;

}public void setBeagleService(BeagleService beagleService) {

this.beagleService = beagleService;

}

public List<MasterAgreement> addDepartmentInExistingCDEAAgreementsInGroup(List<MasterAgreement> cdeaAgreements, String departmentPartyNo) {

List<MasterAgreement> updatedCDEAAgreements = new ArrayList<MasterAgreement>();

Entity entity = Entity.find(repository, Long.parseLong(departmentPartyNo));

for (MasterAgreement masterAgreement : cdeaAgreements) {

if (null != departmentPartyNo && masterAgreement.getCounterpartyCoveredOffice(entity) == null) {

masterAgreement.addCounterpartyCoveredOffice(AgreementHelper.createCoveredOffice(masterAgreement, Entity.find(repository, Long.parseLong(departmentPartyNo)), Party.COUNTERPARTY));

updatedCDEAAgreements.add(masterAgreement);

}

}

return  updatedCDEAAgreements;

}

public void addMissingDepartmentToAgreement(List<MasterAgreement> cdeaMasterAgreements, String departmentPartyNo) {

Entity departmentEntity = Entity.find(repository, Long.parseLong(departmentPartyNo));for (MasterAgreement masterAgreement : cdeaMasterAgreements) {

masterAgreement.addCounterpartyCoveredOffice(AgreementHelper.createCoveredOffice(masterAgreement, departmentEntity, Party.COUNTERPARTY));

LOGGER.debug("CDEA Master Agreement {} department added successfully.", masterAgreement.getId());

}

}

@Override

public void sendMailNotificationForMaInNegotiation() {

if(!checkIfJobIsExeOnAnyNode(MA_IN_NEGO_JOB_ID)) {

makeJobExecutionEntry(node, MA_IN_NEGO_JOB_ID, MA_IN_NEGO_JOB_NAME, MA_IN_NEGO_JOB_DESC);

LOGGER.info("Started Job execution for - Sending Mail To Negotiator for agreements in Nego status on " + node);

List<Object[]> list = repository.getAgreementsInNego();

sendMail(MAIL_MA_IN_NEGO, list);

LOGGER.info("Completed Sending Mail To Negotiator for Ready To sign agreements execution on " +node);

}

}

@Override

public void sendMailNotificationForFileSizeZero() {

if(!checkIfJobIsExeOnAnyNode(FILE_SIZE_ZERO_JOB_ID)) {

makeJobExecutionEntry(node, FILE_SIZE_ZERO_JOB_ID, FILE_SIZE_ZERO_JOB_NAME, FILE_SIZE_ZERO_JOB_DESC);

LOGGER.info("Started Job execution for - Sending Mail To user for file size zero reports on " + node);

String firstDateOfPreviousMonth = firstDateOfPreviousMonth();

String lastDateofPreviousMonth = lastDateofPreviousMonth();

List<EmailConfiguration> emaiIdList = repository.getToMailIdListForAdmLocAndMailTrigger("Report on files with size 0");

List<Object[]> dataListForReport = repository.getDataForFileWithZeroSizeForReport(firstDateOfPreviousMonth,lastDateofPreviousMonth);

for(EmailConfiguration emailConfiguration : emaiIdList){

Boolean lonLoc = false;

Boolean oalLoc = false;

Boolean parNykLoc = false;

Boolean alpLoc = false;

Boolean aleLoc = false;

Boolean giveUpTabLoc1 = false;

Boolean giveUpTabLoc2 = false;

List<String> ccListForFileSizeZeroRec = new ArrayList<>();

String location=new String();

emailConfDeatilsForFileSizeZero(emailConfiguration,lonLoc,oalLoc,parNykLoc,alpLoc,aleLoc,giveUpTabLoc1,giveUpTabLoc2,ccListForFileSizeZeroRec,location,dataListForReport,emaiIdList);

}

}

}

public String firstDateOfPreviousMonth(){

Calendar cal = Calendar.getInstance();

cal.add(Calendar.MONTH, -1);

cal.set(Calendar.DATE,1);

Date firstDateOfPreviousMonth = cal.getTime();

Date date = new Date();

SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

return formatter.format(firstDateOfPreviousMonth);

}

public String lastDateofPreviousMonth(){

Calendar cal = Calendar.getInstance();

cal.add(Calendar.MONTH, -1);

int lastDateOfPreviousMonth = cal.getActualMaximum(Calendar.DATE);

cal.set(Calendar.DATE, lastDateOfPreviousMonth);

Date lastDateOfPreviousMonthDate = cal.getTime();

Date date = new Date();

SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");

return formatter.format(lastDateOfPreviousMonthDate);

}

public void emailConfDeatilsForFileSizeZero(EmailConfiguration emailConfiguration,boolean lonLoc,boolean oalLoc,boolean parNykLoc,boolean alpLoc,boolean aleLoc,boolean giveUpTabLoc1,boolean giveUpTabLoc2,

List <String> ccListForFileSizeZeroRec,String location, List<Object[]> dataListForReport, List<EmailConfiguration> emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"LON")){

location = "LON,GLA";

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

lonLoc = true;

} else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"PAR") ||

StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"NYK")){

location=emailConfiguration.getLocationCode();

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

parNykLoc=true;

}else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"OAL")){

location="LON,GLA,PAR,NYK";

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

oalLoc = true;

}else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"ALP")){

location=emailConfiguration.getLocationCode();

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

alpLoc = true;

}

else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"AG1")){

location=emailConfiguration.getLocationCode();

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

giveUpTabLoc1 = true;

}

else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"AG2")){

location=emailConfiguration.getLocationCode();

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

giveUpTabLoc2 = true;

}

else if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"ALE")){

ccListForFileSizeZeroRec = Arrays.asList(emailConfiguration.getMailAddress().split(","));

location=emailConfiguration.getLocationCode();

aleLoc = true;

}

try{

creatingExcelFileForFileSizeZero(dataListForReport,lonLoc,oalLoc,parNykLoc,alpLoc,aleLoc,giveUpTabLoc1,giveUpTabLoc2,ccListForFileSizeZeroRec,location,emailIdList);

}catch (Exception e){

LOGGER.error("emailConfDeatilsForFileSizeZero Exception is",e);

}

}

public void creatingExcelFileForFileSizeZero(List<Object[]> dataListForReport,boolean lonLoc,boolean oalLoc,boolean parNykLoc,boolean alpLoc,boolean aleLoc,boolean giveUpTabLoc1,boolean giveUpTabLoc2,

List <String> ccListForFileSizeZeroRec,String location, List<EmailConfiguration> emailIdList){

List<Object[]> dataListBaseedOnLoc = new ArrayList();

String tabName = null;

if(dataListForReport.size()>0) {

for (Object[] arr : dataListForReport) {

if(parNykLoc){

if (StringUtils.contains(location, (arr[7]==null)?null:arr[7].toString())) {

if(!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(!StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="allTabExcludingGiveUp";

dataListBaseedOnLoc.add(arr);

}

}

}

}

else if(lonLoc){

if (StringUtils.contains(location, (arr[7]==null)?null:arr[7].toString())) {

if(!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(!StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="allTabExcludingGiveUp";

dataListBaseedOnLoc.add(arr);

}

}

}

}

else if(oalLoc){

if (!StringUtils.contains(location, (arr[7]==null)?null:arr[7].toString())) {

if(!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(!StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="allTabExcludingGiveUp";

dataListBaseedOnLoc.add(arr);

}

}

}

}else if(alpLoc){

if(AgreementCategoryHelper.isPBCategory((arr[16]==null)?null:arr[16].toString())){

if(!StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="allTabExcludingGiveUp";

dataListBaseedOnLoc.add(arr);

}

}

}else if(giveUpTabLoc1){

if(AgreementCategoryHelper.isDERIVCategory((arr[16]==null)?null:arr[16].toString())){

if(StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"UK") ||

StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"UNITED KINGDOM")){

if(StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="GiveUpTab1";

dataListBaseedOnLoc.add(arr);

}

}

}

}else if(giveUpTabLoc2){

if(AgreementCategoryHelper.isDERIVCategory((arr[16]==null)?null:arr[16].toString())){

if(StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"US") ||StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"UNITED STATES") ||

StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"CANADA") ||

StringUtils.equalsIgnoreCase((arr[18]==null)?null:arr[18].toString(),"CA")){

if(StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="GiveUpTab2";

dataListBaseedOnLoc.add(arr);

}

}

}

}else if(aleLoc){

if(AgreementCategoryHelper.isESACategory((arr[16]==null)?null:arr[16].toString())){

if(!StringUtils.equalsIgnoreCase((arr[13]==null)?null:arr[13].toString(),"Give Up Tab")){

tabName="allTabExcludingGiveUp";

dataListBaseedOnLoc.add(arr);

}

}

}

}

if(dataListBaseedOnLoc.size()>0){

generateExcelForNotificationForFileSizeZero(dataListBaseedOnLoc,ccListForFileSizeZeroRec,location,tabName,emailIdList);

}

}

}

public void generateExcelForNotificationForFileSizeZero(List<Object[]> list,List<String> ccListForFileSizeZeroRec,String location,String tabName, List<EmailConfiguration> emailIdList){

XSSFWorkbook workbook = new XSSFWorkbook();

XSSFSheet sheet = workbook.createSheet("Report");

Map<Integer, String[]> data = new TreeMap<Integer, String[]>(buildMapForFileSizeZeroReport(list,tabName));

int rownum = 0;

for (Map.Entry<Integer, String[]> entry : data.entrySet()) {

XSSFRow row = sheet.createRow(rownum++);

String[] strArr = entry.getValue();

int cellnum = 0;

for (String obj : strArr) {

Cell cell = row.createCell(cellnum++);

cell.setCellValue(obj);

}

}

try {

sheet.autoSizeColumn(0);

sheet.autoSizeColumn(1);

SimpleDateFormat formatter = new SimpleDateFormat("MMM");

Date date = new Date();

String createdfileName = "ReportFor"+formatter.format(date)+".xlsx";

File f = File.createTempFile(createdfileName, ".xlsx");

try (FileOutputStream out = new FileOutputStream(f);) {

workbook.write(out);

sendMail(list,f,createdfileName,ccListForFileSizeZeroRec,location,tabName,emailIdList);

} catch (Exception e) {

LOGGER.error("Exception is generateExcelForNotificationForFileSizeZero ",e);

}}catch (Exception e){

LOGGER.error("Exception is generateExcelForNotificationForFileSizeZero ",e);

}

}

private Map<Integer, String[]> buildMapForFileSizeZeroReport(List<Object[]> list,String tabName) {

Map<Integer, String[]> excelMap = new TreeMap<Integer, String[]>();

if(StringUtils.equalsIgnoreCase(tabName,"GiveUpTab1") || StringUtils.equalsIgnoreCase(tabName,"GiveUpTab2")){

excelMap.put(1, new String[]{"MA ID", "MA GROUP ID","MA TYPE","MA Agreement Date","MA Status","BNP Entity",

"Negotiator First/Last Name","Negotiator Admin Location",

"Uploaded First/Last Name","Upload Date","Document Id","Document Name","Screen"});

}else{

excelMap.put(1, new String[]{"MA ID", "MA GROUP ID","MA TYPE","MA Agreement Date","MA Status","BNP Entity",

"Negotiator First/Last Name","Negotiator Admin Location",

"Uploaded First/Last Name","Upload Date","Document Id","Document Name","Document Type","Screen"});

}

int key = 2;

for (Object[] arr : list) {if(StringUtils.equalsIgnoreCase(tabName,"GiveUpTab1") || StringUtils.equalsIgnoreCase(tabName,"GiveUpTab2")){

excelMap.put(key+1, new String[]{arr[0] == null ? null : arr[0].toString(),arr[1] == null ? null : arr[1].toString(),

arr[2] == null ? null : arr[2].toString(),arr[3] == null ? null : arr[3].toString(),

arr[4] == null ? null : arr[4].toString(),arr[5] == null ? null : arr[5].toString(),

arr[6] == null ? null : arr[6].toString(),arr[7] == null ? null : arr[7].toString(),

arr[8] == null ? null : arr[8].toString(), arr[9] == null ? null : arr[9].toString(),

arr[10] == null ? null : arr[10].toString(),arr[11] == null ? null : arr[11].toString(),

arr[13] == null ? null : arr[13].toString(),

});

}else{

excelMap.put(key+1, new String[]{arr[0] == null ? null : arr[0].toString(),arr[1] == null ? null : arr[1].toString(),

arr[2] == null ? null : arr[2].toString(),arr[3] == null ? null : arr[3].toString(),

arr[4] == null ? null : arr[4].toString(),arr[5] == null ? null : arr[5].toString(),

arr[6] == null ? null : arr[6].toString(),arr[7] == null ? null : arr[7].toString(),

arr[8] == null ? null : arr[8].toString(), arr[9] == null ? null : arr[9].toString(),

arr[10] == null ? null : arr[10].toString(),arr[11] == null ? null : arr[11].toString(),

arr[12] == null ? null : arr[12].toString(),arr[13] == null ? null : arr[13].toString(),

});

}

key = key+1;

}

return excelMap;

}

@Override

public void sendNegotiatorMailPendingToFinalEntityMA(){

if(!checkIfJobIsExeOnAnyNode(MA_RTS_JOB_ID)) {

makeJobExecutionEntry(node, MA_RTS_JOB_ID, MA_RTS_JOB_NAME, MA_RTS_JOB_DESC);

LOGGER.info("Started Job execution for - Sending Mail To Negotiator for Ready To sign agreements on " + node);

List<Object[]> list = repository.getAgreementsForFinalEntites();

LOGGER.info("Sending Mail To Negotiator for Ready To sign agreements");

sendMail(MAIL_MA_ENTITY_FINAL, list);

LOGGER.info("Completed Job execution for - Sending Mail To Negotiator for Ready To sign agreements on " +node);

}

}

@Override

public void sendMailForMaAndCollateralInNegotiation(){

if(!checkIfJobIsExeOnAnyNode(MA_COLLATERAL_IN_NEGO_JOB_ID)) {

makeJobExecutionEntry(node, MA_COLLATERAL_IN_NEGO_JOB_ID, MA_COLLATERAL_IN_NEGO_JOB_NAME, MA_COLLATERAL_IN_NEGO_JOB_DESC);

LOGGER.info("Started Job execution for - send mail for Agreement and Collateral In Nego on " + node);

List<Object[]> list = repository.getAgreementsAndCollateralInNego();

sendMail(MAIL_MA_COLLATERAL_IN_NEGO, list);

LOGGER.info("Completed Job execution for - send mail for Agreement and Collateral In Nego on " +node);

}

}

private boolean checkIfJobIsExeOnAnyNode(String jobId){

return repository.findIfJobIsExecutedOnAnyNode(jobId);

}

public void makeJobExecutionEntry(String node, String jobId, String jobName, String jobDesc){

dailySchedularExeStatus.setNode(node);

dailySchedularExeStatus.setJobId(jobId);

dailySchedularExeStatus.setJobDate(new Date());

dailySchedularExeStatus.setJobName(jobName);

dailySchedularExeStatus.setJobDescription(jobDesc);

repository.save("DailySchedularExeStatus",dailySchedularExeStatus);

}

private void sendMail(String mailCategory, List<Object[]> list){

String negotiatorMailId;

String adminLocation;

String maId;

String grpId;

MailService mailService = applicationContext.getBean(MailService.class);

for (Object[] arr : list) {

grpId = arr[0] == null ? null : arr[0].toString();

maId =  arr[1] == null ? null : arr[1].toString();

negotiatorMailId = (String) arr[2];

if(MAIL_MA_IN_NEGO.equals(mailCategory))

{

adminLocation = (String) arr[3];

mailService.sendMailForAgreementsInNego(grpId, maId, negotiatorMailId, adminLocation);

}

else if(MAIL_MA_ENTITY_FINAL.equals(mailCategory)){

mailService.sendMailForFinalEntityMA(grpId, maId, negotiatorMailId, null);

}

else if (MAIL_MA_COLLATERAL_IN_NEGO.equals(mailCategory)){

mailService.sendMailForFinalEntityMA(grpId, maId, negotiatorMailId, null);

}

else if (MAIL_MA_COLLATERAL_IN_NEGO.equals(mailCategory)){

adminLocation = (String) arr[3];

mailService.sendMailForAgreementAndCollateralInNego(grpId,maId,negotiatorMailId,adminLocation);

}

}

}

private void sendMail(List<Object[]> list,File f,String fileName,List<String> ccListForFileSizeZeroRec,String location,String tabName,List<EmailConfiguration> emailIdList) {

List<String> userMailIdList = new ArrayList<>();

MailService mailService = applicationContext.getBean(MailService.class);

userMailIdList = creatingToListForFileSizeZero(list,tabName,emailIdList,userMailIdList);

mailService.sendMailForFileSizeZero(ccListForFileSizeZeroRec,f,fileName,userMailIdList,location,tabName);

}

public List<String> creatingToListForFileSizeZero(List<Object[]> list, String tabName,List<EmailConfiguration> emailIdList,List<String> userMailIdList) {

String location="LON,GLA,PAR,NYK";

for (Object[] arr : list) {if(StringUtils.equalsIgnoreCase((arr[7]==null)?null:arr[7].toString(),"PAR") &&

!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(StringUtils.equalsIgnoreCase(tabName,"allTabExcludingGiveUp")){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"PAU")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}

if((StringUtils.equalsIgnoreCase((arr[7]==null)?null:arr[7].toString(),"LON") ||

StringUtils.equalsIgnoreCase((arr[7]==null)?null:arr[7].toString(),"GLA")) &&

!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(StringUtils.equalsIgnoreCase(tabName,"allTabExcludingGiveUp")){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"LOU")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}if(StringUtils.equalsIgnoreCase((arr[7]==null)?null:arr[7].toString(),"NYK") &&

!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())) {

if (StringUtils.equalsIgnoreCase(tabName, "allTabExcludingGiveUp")) {

for (EmailConfiguration emailConfiguration : emailIdList) {

if (StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(), "NYU")) {

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}

if((!StringUtils.contains(location, (arr[7]==null)?null:arr[7].toString()))&&

!AgreementCategoryHelper.isCategoryRestricted((arr[16]==null)?null:arr[16].toString())){

if(StringUtils.equalsIgnoreCase(tabName,"allTabExcludingGiveUp")){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"OAU")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}if(StringUtils.equalsIgnoreCase((arr[16]==null)?null:arr[16].toString(),"PB")){

if(StringUtils.equalsIgnoreCase(tabName,"allTabExcludingGiveUp")){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"PBU")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}else if(StringUtils.equalsIgnoreCase((arr[16]==null)?null:arr[16].toString(),"ESA")) {

if (StringUtils.equalsIgnoreCase(tabName, "allTabExcludingGiveUp")) {

for (EmailConfiguration emailConfiguration : emailIdList) {

if (StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(), "ESU")) {

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}else if(StringUtils.equalsIgnoreCase(tabName,"GiveUpTab1") &&

AgreementCategoryHelper.isDERIVCategory((arr[16]==null)?null:arr[16].toString())){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"GU1")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}else if(StringUtils.equalsIgnoreCase(tabName,"GiveUpTab2") &&

AgreementCategoryHelper.isDERIVCategory((arr[16]==null)?null:arr[16].toString())){

for(EmailConfiguration emailConfiguration : emailIdList){

if(StringUtils.equalsIgnoreCase(emailConfiguration.getLocationCode(),"GU2")){

userMailIdList = getUserMailIdList(arr, emailConfiguration, userMailIdList);

}

}

}

}

for (String userMailId : userMailIdList) {

if (!userMailIdList.contains(userMailId)) {

userMailIdList.add(userMailId);

}

}

return userMailIdList;

}

private static List<String> getUserMailIdList(Object[] arr, EmailConfiguration emailConfiguration, List<String> userMailIdList) {

if(!StringUtils.equalsIgnoreCase(emailConfiguration.getMailAddress(),"$UPLOADER")){

userMailIdList = Arrays.asList(emailConfiguration.getMailAddress().split(","));

}else{

if(!userMailIdList.contains((arr[17]==null)?null: arr[17].toString())) {

userMailIdList.add((arr[17]==null)?null: arr[17].toString());

}

}

return userMailIdList;

}

@Override

public void backgroundTasks(){

Thread.currentThread().setName("GEJ-Scheduler");

GroupEditJobExecutor executor = new GroupEditJobExecutor(repository);

if (executor.isBackgroundProcessingEnabled()) {

List<GroupEditJob> jobs;

GroupEditJobNotification notification = new GroupEditJobNotification(negotiationService.getMailService());

try {

LOGGER.info("Checking jobs");

jobs = groupEditJobService.getReadyJobs(groupEditJobService.getProcessingGroupId());

if (CollectionUtils.isNotEmpty(jobs)) {

LOGGER.info("Ready jobs: " + jobs.size() + " " + jobs.stream().map(GroupEditJob::getId).collect(Collectors.toList()));

Optional<GroupEditJob> toProcessJobOptional = jobs.stream().filter(job -> !groupEditJobService.isProcessing(job)).findFirst();

if (toProcessJobOptional.isPresent() && !groupEditJobService.getProcessingGroupId().contains(toProcessJobOptional.get().getGroupId())) {

GroupEditJob job = toProcessJobOptional.get();

processGroupEditJob(notification, job);

}

}

} catch (Exception e) {

LOGGER.error("Error occurred for backgroundTasks", e);

}

}

}

private void processGroupEditJob(GroupEditJobNotification notification, GroupEditJob job) {

try {

LOGGER.info("Picked " + job.getOperation().getClassName() + " of Group " + job.getGroupId() + " for processing: " + job.getId());

final Class actionClass = Class.forName(job.getOperation().getClassName());

Operation operation = findOperation(actionClass);

job.getParameters().masterAgreement = MasterAgreement.get(repository, job.getParameters().masterAgreement.getId());

auditor.openRequest(job.getUser());

groupEditJobService.updateInProgressStatus(job);

try{

operation.execute(job.getParameters());

LOGGER.info("Operation class execution completed " + job.getId());

}catch(Exception exc){

LOGGER.error("Error occurred while executing job " + job.getId(), exc);

failGroupEditJob(notification, job, exc);

}

groupEditJobService.updateCompletedStatus(job);

LOGGER.info("Job " + job.getId() + " of Group " + job.getGroupId() + " Completed");

} catch (Exception ex) {

LOGGER.error("Error occurred for job " + job.getId(), ex);

failGroupEditJob(notification, job, ex);

}

}

private void failGroupEditJob(GroupEditJobNotification notification, GroupEditJob job, Exception ex) {

try {

GroupEditFailedJob groupEditFailedJob = new GroupEditFailedJob(job, ExceptionUtils.getMessage(ex), ExceptionUtils.getRootCauseMessage(ex));

groupEditJobService.updateErrorStatus(job, groupEditFailedJob);

notification.sendFailureMail(job, ExceptionUtils.getStackTrace(ex));

} catch (Exception e) {

LOGGER.error("Exception while trying to save failed job", e);

notification.sendFailureMail(job, ExceptionUtils.getStackTrace(ex) + "\n\n Exception while trying to save failed job:\n" + ExceptionUtils.getRootCauseMessage(e));

}

}

public void moveGroupEditFailedJob(){

try {

if (!checkIfJobIsExeOnAnyNode(MOVE_GRP_EDIT_FAILED_JOB_ID)) {

makeJobExecutionEntry(node, MOVE_GRP_EDIT_FAILED_JOB_ID, MOVE_GRP_EDIT_FAILED_JOB_NAME, MOVE_GRP_EDIT_FAILED_JOB_DESC);

LOGGER.info("Started Job execution for - Moving Group Edit Failed Job on " + node);

repository.updateGroupEditFailedJob();

LOGGER.info("Completed Job execution for - Moving Group Edit Failed Job on " + node);

}

}catch(Exception e){

e.printStackTrace();

}

}

public void moveGroupEditCompletedJob(){

if(!checkIfJobIsExeOnAnyNode(MOVE_GRP_EDIT_COMPLETED_JOB_ID)) {

makeJobExecutionEntry(node, MOVE_GRP_EDIT_COMPLETED_JOB_ID, MOVE_GRP_EDIT_COMPLETED_JOB_NAME, MOVE_GRP_EDIT_COMPLETED_JOB_DESC);

LOGGER.info("Started Job execution for - Moving Group Edit Failed Job on " + node);

repository.updateGroupEditCompletedJob();

LOGGER.info("Completed Job execution for - Moving Group Edit Failed Job on " +node);

}

}

public void deleteOldCompletedGroupEditJobs(){

if(!checkIfJobIsExeOnAnyNode(DELETE_GRP_EDIT_OLD_COMPLETED_JOB_ID)) {

makeJobExecutionEntry(node, DELETE_GRP_EDIT_OLD_COMPLETED_JOB_ID, DELETE_GRP_EDIT_OLD_COMPLETED_JOB_NAME, DELETE_GRP_EDIT_OLD_COMPLETED_JOB_DESC);

LOGGER.info("Started Job execution for - Deleting Group Edit Old Completed Job on " + node);

repository.deleteGroupEditCompletedJob();

LOGGER.info("Completed Job execution for - Deleting Group Edit Old Completed Job on " +node);

}

}

public void deleteOldFailedGroupEditJobs(){

if(!checkIfJobIsExeOnAnyNode(DELETE_GRP_EDIT_OLD_FAILED_JOB_ID)) {

makeJobExecutionEntry(node, DELETE_GRP_EDIT_OLD_FAILED_JOB_ID, DELETE_GRP_EDIT_OLD_FAILED_JOB_NAME, DELETE_GRP_EDIT_OLD_FAILED_JOB_DESC);

LOGGER.info("Started Job execution for - Deleting Group Edit Old Failed Job on " + node);

repository.deleteGroupEditFailedJob();

LOGGER.info("Completed Job execution for - Deleting Group Edit Old Failed Job on " +node);

}

}

@Override

public void backgroundAutoExecuteAgreement(){

if(!checkIfJobIsExeOnAnyNode(AUTO_EXECUTE_RTS_MA_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_EXECUTE_RTS_MA_JOB_ID, AUTO_EXECUTE_RTS_MA_JOB_NAME, AUTO_EXECUTE_RTS_MA_JOB_DESC);

List<ReadyToSignAutoExecute> jobs, deletedNegotiatorJobs;

AutoExecuteAgreementNotification autoExecuteAgreementNotification = new AutoExecuteAgreementNotification(negotiationService.getMailService());

try {

LOGGER.info("Fetching agreements in Ready to Sign state for auto execution : Non-Active Negotiator");

deletedNegotiatorJobs = autoExecuteAgreementService.getReadyToSignJobs(false);

if (CollectionUtils.isNotEmpty(deletedNegotiatorJobs)) {

for (ReadyToSignAutoExecute rtsAutoExecuteJob : deletedNegotiatorJobs) {

failedToAutoExecuteForInvalidNegotiator(autoExecuteAgreementNotification, rtsAutoExecuteJob);

}

}LOGGER.info("Fetching agreements in Ready to Sign state for auto execution");

jobs = autoExecuteAgreementService.getReadyToSignJobs(true);

if (CollectionUtils.isNotEmpty(jobs)) {

LOGGER.info("Number of Ready to Sign agreements to be auto executed: " + jobs.size());

for (ReadyToSignAutoExecute readyToSignAutoExecuteJob : jobs) {

if (readyToSignAutoExecuteJob.getGroupId() == null) {

getProcessMAAutoExecution(autoExecuteAgreementNotification, readyToSignAutoExecuteJob);

} else {

getProcessGroupMAAutoExecution(autoExecuteAgreementNotification, readyToSignAutoExecuteJob);

}

}

}

} catch (Exception e) {

LOGGER.error("Error occurred during auto execution of Ready to Sign agreements", e);

}

}

}

private void getProcessMAAutoExecution(AutoExecuteAgreementNotification autoExecuteAgreementNotification, ReadyToSignAutoExecute readyToSignAutoExecutejob) {

try {

Entity cpEntity = null;

ExecuteMasterAgreementOperation executeAgreementOperation = (ExecuteMasterAgreementOperation) applicationContext.getBean("ExecuteMasterAgreementOperation");

ExecuteMasterAgreementOperation.MyParameters parameters = new ExecuteMasterAgreementOperation.MyParameters();

parameters.signatureDate = readyToSignAutoExecutejob.getAgreementDate();

parameters.masterAgreement = MasterAgreement.get(repository, readyToSignAutoExecutejob.getMaId());

parameters.amendment = repository.findMaster(readyToSignAutoExecutejob.getMaId());

auditor.openRequest(parameters.masterAgreement.getAmendment(0).getNegotiator().getUser());

cpEntity = parameters.masterAgreement.getCounterpartyEntity();

parameters.bnpParibasCreditEventUponMerger = BooleanUtil.isTrue(parameters.masterAgreement.getBnpParibasCreditEventUponMerger());

parameters.counterpartyCreditEventUponMerger = BooleanUtil.isTrue(parameters.masterAgreement.getCounterpartyCreditEventUponMerger());

parameters.isAutoExecuteflag = true;

if ((!parameters.masterAgreement.getAmendment(0).getMasterAgreement().isExecuted())

&& ("Y".equalsIgnoreCase(readyToSignAutoExecutejob.getReadyToSign()))

&& (cpEntity.isActive())

&& (!cpEntity.isPending())) {

LOGGER.info("Auto Execution of Master Agreement " + readyToSignAutoExecutejob.getMaId() + " is started");

if(getOverlapCheckExecuted(parameters.masterAgreement, parameters.amendment, autoExecuteAgreementNotification)) {

executeAgreementOperation.execute(parameters);

autoExecuteAgreementService.updateAutoExecutedStatus(readyToSignAutoExecutejob);

sendAutoExecutedStatusSuccessMail(parameters.amendment, autoExecuteAgreementNotification);

LOGGER.info("Auto Execution of Master Agreement " + readyToSignAutoExecutejob.getMaId() + " is completed");

}

}

} catch (Exception ex) {

LOGGER.error("Error occurred during processing of auto execution of Ready to Sign agreement "+readyToSignAutoExecutejob.getMaId(), ex);autoExecuteAgreementService.updateAutoExecuteExceptionDetails(ExceptionUtils.getMessage(ex), ExceptionUtils.getRootCauseMessage(ex),

readyToSignAutoExecutejob.getMaId().toString(), null);

}

}

private void getProcessGroupMAAutoExecution(AutoExecuteAgreementNotification autoExecuteAgreementNotification, ReadyToSignAutoExecute readyToSignAutoExecutejob) {

try {

Entity cpEntity = null;

ExecuteGroupOperation executeGroupMAOperation = (ExecuteGroupOperation) applicationContext.getBean("ExecuteGroupOperation");

ExecuteGroupOperation.MyParameters parameters = new ExecuteGroupOperation.MyParameters();

parameters.signatureDate = readyToSignAutoExecutejob.getAgreementDate();

parameters.masterAgreement = MasterAgreement.get(repository, readyToSignAutoExecutejob.getMaId());

parameters.amendment = repository.findMaster(readyToSignAutoExecutejob.getMaId());

RequiredFieldAnalyser analyser = new RequiredFieldAnalyser(repository);

analyser.setMasterAgreementId(readyToSignAutoExecutejob.getMaId());parameters.requirementsList = Collections.singletonList(analyser);

auditor.openRequest(parameters.masterAgreement.getAmendment(0).getNegotiator().getUser());

cpEntity = parameters.masterAgreement.getCounterpartyEntity();

parameters.isAutoExecuteflag = true;

if ((!parameters.masterAgreement.getAmendment(0).getMasterAgreement().isExecuted())

&& ("Y".equalsIgnoreCase(readyToSignAutoExecutejob.getReadyToSign()))

&& (cpEntity.isActive())

&& (!cpEntity.isPending())) {

LOGGER.info("Auto Execution of Group Master Agreement " + readyToSignAutoExecutejob.getMaId() + " is started");

if(getOverlapCheckExecuted(parameters.masterAgreement, parameters.amendment, autoExecuteAgreementNotification)) {

executeGroupMAOperation.execute(parameters);

autoExecuteAgreementService.updateAutoExecutedStatus(readyToSignAutoExecutejob);

sendAutoExecutedStatusSuccessMail(parameters.amendment, autoExecuteAgreementNotification);

LOGGER.info("Auto Execution of Group Master Agreement " + readyToSignAutoExecutejob.getMaId() + " is completed");

}

}

} catch (Exception ex) {

LOGGER.error("Error occurred during processing of auto execution of Ready to Sign Group agreement "+readyToSignAutoExecutejob.getMaId(), ex);

autoExecuteAgreementService.updateAutoExecuteExceptionDetails(ExceptionUtils.getMessage(ex), ExceptionUtils.getRootCauseMessage(ex),

readyToSignAutoExecutejob.getMaId().toString(), readyToSignAutoExecutejob.getGroupId().toString());

}

}

private boolean getOverlapCheckExecuted(MasterAgreement ma, Amendment amendment, AutoExecuteAgreementNotification notification){

String maId;

String groupId;

String adminLoc;

List<MAStatusInfo> maStatusInfoList = autoExecuteAgreementService.doProcessOverlap(ma);

OverlapProductInfoOperation overlapProductInfoOperation = (OverlapProductInfoOperation) applicationContext.getBean("OverlapProductInfoOperation");

OverlapProductInfoOperation.MyParameters p = new OverlapProductInfoOperation.MyParameters();

p.masterAgreement = ma;

if(maStatusInfoList != null){

if((ma != null) && (ma.getOverlappedProdComm() == null)) {

maId = ma.getId() != null ? ma.getId().toString() : null;

groupId = ma.getGroup() != null ? ma.getGroup().getId().toString() : null;

adminLoc = amendment.getAdminLocation().getName();

repository.getAutoExecutestatusUpdated(Long.parseLong(maId), "Overlap found during MA execution");

notification.sendAutoExecutionOverlapMail(amendment, groupId, maId, maStatusInfoList, adminLoc);

LOGGER.info("Auto Execution of Master Agreement " + maId + " is skipped as is Overlap found.");

return false;

}

}else{

if((ma != null) && (ma.getOverlappedProdComm() != null)) {

p.setHasOverlap(true);

p.setAcknowledge(false);

p.setAckComment(null);

overlapProductInfoOperation.execute(p);

return true;

}

}

return true;

}

private void sendAutoExecutedStatusSuccessMail(Amendment amendment, AutoExecuteAgreementNotification notification) {

try {

notification.sendSuccessMail(amendment);

} catch (Exception e) {

LOGGER.error("Exception while sending Auto Execution success notification for RTS agreements", e);

}

}

public void setLeiService(LeiService leiService) {

this.leiService = leiService;

}

private void failedToAutoExecuteForInvalidNegotiator(AutoExecuteAgreementNotification notification, ReadyToSignAutoExecute job) {

try {

String maId,groupId;

Entity cpEntity = null;

ExecuteMasterAgreementOperation.MyParameters parameters = new ExecuteMasterAgreementOperation.MyParameters();

parameters.masterAgreement = MasterAgreement.get(repository, job.getMaId());

parameters.amendment = repository.findMaster(job.getMaId());

cpEntity = parameters.masterAgreement.getCounterpartyEntity();

if ((!parameters.masterAgreement.getAmendment(0).getMasterAgreement().isExecuted())

&& ("Y".equalsIgnoreCase(job.getReadyToSign()))&& (cpEntity.isActive())

&& (!cpEntity.isPending())) {

maId = parameters.masterAgreement.getId() != null ? parameters.masterAgreement.getId().toString() : null;

groupId = parameters.masterAgreement.getGroup() != null ? parameters.masterAgreement.getGroup().getId().toString() : null;

autoExecuteAgreementService.updateAutoExecuteExceptionDetails(null, DETAIL_ERR_MSG, maId, groupId);

notification.sendAutoExecuteFailureMail(parameters.amendment, job);

}

} catch (Exception e) {

LOGGER.error("Exception while trying to save failed job", e);

}

}

public boolean convertFileFromCsvToExcel(String xlsxFileAddress, String csvFileAddress, String protocol) {

BufferedReader br = null;

InputStreamReader inputStreamReader = null;

if(getFile(csvFileAddress).exists()) {

LOGGER.info("File conversion started successfully for protocol - "+ protocol);

try (XSSFWorkbook workBook = new XSSFWorkbook()) {

XSSFSheet sheet = workBook.createSheet("sheet1");

String currentLine = null;

int rowNum = -1;inputStreamReader = new InputStreamReader( new FileInputStream(new File(csvFileAddress)),"UTF-8");

br = new BufferedReader(inputStreamReader);

while ((currentLine = br.readLine()) != null) {

String[] str = currentLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

rowNum++;

XSSFRow currentRow = sheet.createRow(rowNum);

for (int i = 0; i < str.length; i++) {

currentRow.createCell(i).setCellValue(str[i].replaceAll("\"","").trim());

}

}

FileOutputStream fileOutputStream = new FileOutputStream(xlsxFileAddress);

workBook.write(fileOutputStream);

fileOutputStream.close();

LOGGER.info("File conversion completed successfully for protocol - "+ protocol);

return true;

} catch (Exception ex) {

LOGGER.error(FILE_CONVERSION_EXCEPTION, ex);

} finally {

try {

if(br != null) {

br.close();

}

if(inputStreamReader != null) {

inputStreamReader.close();

}

} catch (IOException e) {

LOGGER.error(FILE_CONVERSION_EXCEPTION, e);

}

}

}else {

LOGGER.info("No (.csv) file found for protocol - "+ protocol);

sendMailForMissingISDAFile(protocol);

return false;

}

return false;

}

public boolean performPreUploadChecks(File file, FileUpload fileUpload, String uploadType){

File uploadedFile = null;

String fileName = null;

Workbook book;

String extn;

List<Attachment> attachments = new ArrayList();

Attachment attachment = new Attachment(file, file.getName(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", null, "file");

attachments.add(attachment);

for (Attachment attach : attachments) {

uploadedFile = attach.getFile();

fileName = attach.getFilename();

}

try {

if((fileName != null && StringUtils.isNotBlank(fileName))) {

int endIndex = fileName.length();

int startIndex = fileName.lastIndexOf('.') + 1;

extn = fileName.substring(startIndex, endIndex);

book = loadWorkBook(uploadedFile, extn);

}else{

return false;

}

if ("xls".equalsIgnoreCase(extn.toLowerCase())) {

fileUpload.setWb(book);

}

if ("xlsx".equalsIgnoreCase(extn.toLowerCase())) {

if (fileUpload instanceof DFFileUpload) {

fileUpload.setUploadedFile(uploadedFile);

DFFileUpload dfFileUpload = (DFFileUpload) fileUpload;

dfFileUpload.setPkg(OPCPackage.open(uploadedFile, PackageAccess.READ));

dfFileUpload.setUploadType(uploadType);

} else {

fileUpload.setWb(book);

}

}

}catch(InvalidFormatException e) {

LOGGER.error("Unable to upload the file", e);

}catch (Exception ex) {

LOGGER.error("Unable to upload the file", ex);

}

fileUpload.setApplicationContext(applicationContext);

fileUpload.setAuditor(auditor);

fileUpload.setBeagleRepository(repository);

fileUpload.setBeagleDataSource(beagleDataSource);

return true;

}

private Workbook loadWorkBook(File uploadedFile, String extn) {

Workbook book = null;

try(InputStream fis = new FileInputStream(uploadedFile)) {

if("xls".equalsIgnoreCase(extn.toLowerCase())) {

book = new HSSFWorkbook(fis);

}else{

book = new XSSFWorkbook(fis);

}

} catch (FileNotFoundException e) {LOGGER.error(FILE_NOT_FOUND, e);

} catch (IOException e) {

LOGGER.error(LOAD_EXCEPTION, e);

}

return book;

}

private File getFile(String xlsxFileAddress){

return new File(xlsxFileAddress);

}

public void performTask(String protocol, String xlsxFileAddress){

FileUpload fileUpload = null;

try {

if ((ISDA_BMR.equalsIgnoreCase(protocol))) {

fileUpload = new IsdaBmrFileUpload(getFile(xlsxFileAddress).getName());

}else if ((ISDA_RSP.equalsIgnoreCase(protocol))) {

fileUpload = new IsdaRspFileUpload(getFile(xlsxFileAddress).getName());

}else if ((ISDA_JMP.equalsIgnoreCase(protocol))) {

fileUpload = new IsdaJmpFileUpload(getFile(xlsxFileAddress).getName());

}else if ((ISDA_BAIL_IN.equalsIgnoreCase(protocol))) {

fileUpload = new IsdaBailInArticleFileUpload(getFile(xlsxFileAddress).getName());

} else if ((BRRDII_RSP.equalsIgnoreCase(protocol))) {

fileUpload = new BrrdFileUpload(getFile(xlsxFileAddress).getName());

}else if ((ISDA_PRDR.equalsIgnoreCase(protocol))) {fileUpload = new IsdaPRDRFileUpload(getFile(xlsxFileAddress).getName());

}

if ((fileUpload != null) && (performPreUploadChecks(getFile(xlsxFileAddress), fileUpload, protocol))) {

fileUpload.setAutoFileUpload(true);

fileUpload.execute();

BeagleFileUtils.emptyDirectory(new File(filePath + protocol));

}

}catch(Exception ex){

LOGGER.error("Exception during performTask - auto upload of "+protocol+" file.", ex);

}

}

@Override

public void backgroundAutoUploadBMR() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+ISDA_BMR+".sh";

String secondPath = isdaBatchPath+ISDA_BMR+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_BMR_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_BMR_JOB_ID, AUTO_UPLOAD_BMR_JOB_NAME, AUTO_UPLOAD_BMR_JOB_DESC);

try {

backgroundShellOperation(ISDA_BMR, firstPath, secondPath);

xlsxFileAddress = filePath +ISDA_BMR+ "/" +ISDA_BMR + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+ISDA_CSV_PATH +"/"+ ISDA_BMR+".csv";if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, ISDA_BMR)) {

performTask(ISDA_BMR, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA BMR file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA BMR file.", ex);

}

}

}

@Override

public void backgroundAutoUploadRSP() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+ISDA_RSP+".sh";

String secondPath = isdaBatchPath+ISDA_RSP+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_RSP_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_RSP_JOB_ID, AUTO_UPLOAD_RSP_JOB_NAME, AUTO_UPLOAD_RSP_JOB_DESC);

try {

backgroundShellOperation(ISDA_RSP, firstPath, secondPath);

xlsxFileAddress = filePath +ISDA_RSP+ "/" +ISDA_RSP + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+ISDA_CSV_PATH +"/"+ISDA_RSP+".csv";

if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, ISDA_RSP)) {

performTask(ISDA_RSP, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA RSP file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA RSP file.", ex);

}

}

}

@Override

public void backgroundAutoUploadPRDR() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+ISDA_PRDR+".sh";

String secondPath = isdaBatchPath+ISDA_PRDR+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_PRDR_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_PRDR_JOB_ID, AUTO_UPLOAD_PRDR_JOB_NAME, AUTO_UPLOAD_PRDR_JOB_DESC);

try {

backgroundShellOperation(ISDA_PRDR, firstPath, secondPath);

xlsxFileAddress = filePath +ISDA_PRDR+ "/" +ISDA_PRDR + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+ISDA_CSV_PATH +"/"+ISDA_PRDR+".csv";if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, ISDA_PRDR)) {

performTask(ISDA_PRDR, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA PRDR file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA PRDR file.", ex);

}

}

}

@Override

public void backgroundAutoUploadJMP() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+ISDA_JMP+".sh";

String secondPath = isdaBatchPath+ISDA_JMP+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_JMP_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_JMP_JOB_ID, AUTO_UPLOAD_JMP_JOB_NAME, AUTO_UPLOAD_JMP_JOB_DESC);

try {

backgroundShellOperation(ISDA_JMP, firstPath, secondPath);

xlsxFileAddress = filePath +ISDA_JMP+ "/" +ISDA_JMP + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+ISDA_CSV_PATH +"/"+ ISDA_JMP +".csv";

if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, ISDA_JMP)) {performTask(ISDA_JMP, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA JMP file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA JMP file.", ex);

}

}

}

@Override

public void backgroundAutoUploadBailInArticle() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+ISDA_BAIL_IN+".sh";

String secondPath = isdaBatchPath+ISDA_BAIL_IN+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_BAIL_IN_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_BAIL_IN_JOB_ID, AUTO_UPLOAD_BAIL_IN_JOB_NAME, AUTO_UPLOAD_BAIL_IN_JOB_DESC);

try {

backgroundShellOperation(ISDA_BAIL_IN, firstPath, secondPath);

xlsxFileAddress = filePath +ISDA_BAIL_IN+ "/" +ISDA_BAIL_IN + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+ISDA_CSV_PATH +"/"+ ISDA_BAIL_IN +".csv";

if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, ISDA_BAIL_IN)) {

performTask(ISDA_BAIL_IN, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA BAIL IN file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA BAIL-IN file.", ex);

}

}

}

@Override

public void backgroundAutoUploadBRRD2() {

String xlsxFileAddress;

String csvFileAddress;

String firstPath = isdaFormURLBatchPath+BRRDII_RSP+".sh";

String secondPath = isdaBatchPath+BRRDII_RSP+".sh";

if (!checkIfJobIsExeOnAnyNode(AUTO_UPLOAD_BRRD2_JOB_ID)) {

makeJobExecutionEntry(node, AUTO_UPLOAD_BRRD2_JOB_ID, AUTO_UPLOAD_BRRD2_JOB_NAME, AUTO_UPLOAD_BRRD2_JOB_DESC);

try {

backgroundShellOperation(BRRDII_RSP, firstPath, secondPath);

xlsxFileAddress = filePath +BRRDII_RSP+ "/" +BRRDII_RSP + new SimpleDateFormat(FILE_PATTERN).format(new Date());

csvFileAddress = filePath+BRRD_CSV_PATH +"/"+ BRRDII_RSP +".csv";

if (convertFileFromCsvToExcel(xlsxFileAddress, csvFileAddress, BRRDII_RSP)) {

performTask(BRRDII_RSP, xlsxFileAddress);

LOGGER.info("Auto upload of ISDA BRRD2 file successful on "+ new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (Exception ex) {

LOGGER.error("Exception during auto upload of ISDA BRRD2 file.", ex);

}

}

}

public void backgroundShellOperation(String protocol, String firstPath, String secondPath){

int count = 1;

BeagleFileUtils.deleteFile(new File(secondPath));

LOGGER.info("Deleted the old Operation.sh file : "+secondPath);

String [] scriptPaths ={firstPath, secondPath};

LOGGER.info("isdaFormURLBatchPath file path : "+firstPath);

for (String shellPath : scriptPaths) {

LOGGER.info("Shell file script "+count+" exists. Path is "+shellPath);

backgroundAutoUpdateUrlAndDownloadCsvFile(protocol, shellPath, count);

count++;

}

}

public void backgroundAutoUpdateUrlAndDownloadCsvFile(String protocol, String path, int count) {

String line;

ProcessBuilder processBuilder = new ProcessBuilder(path);

try {LOGGER.info("Job call "+count+", protocol is "+protocol+". Started on "+new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

Process process = processBuilder.start();

BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

while ((line = reader.readLine()) != null) {

LOGGER.info("Batch command for (.csv) file download: "+line);

}

int exitVal = process.waitFor();

if (exitVal == 0) {

LOGGER.info("Success for batch call "+count+". Protocol is "+protocol+". executed on "+new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

} else {

LOGGER.info("Failure for batch call "+count+" for "+protocol+" on "+new SimpleDateFormat(FORMAT_PATTERN).format(new Date()));

}

} catch (IOException ex) {

LOGGER.error("Exception during call "+count+" call for "+protocol, ex);

} catch (InterruptedException ex) {

LOGGER.error("Exception encountered during call "+count+" for "+protocol, ex);

Thread.currentThread().interrupt();

}

}

private void sendMailForMissingISDAFile(String protocol) {

try {

MailService mailService = applicationContext.getBean(MailService.class);

mailService.sendMailForMissingISDAFile(protocol);

} catch (Exception e) {

LOGGER.error("Exception while sending notification for missing ISDA file(.csv)", e);

}

}

@SuppressWarnings("unchecked")

@Override

public void backgroundTradeReconstructionFileProcess() {

String url="No Url";

File[] files= TradeReconstructionUtils.getListOfFiles(tradeReconstructionInputPath);

if(files!=null && files.length>0) {

File[] sortedfiles = TradeReconstructionUtils.sortFilesByDate(files);

url=tradeReconstructionUtils.getHyperLinkUrl(prodUrl,stagingUrl,uatUrl);

tradeReconstructionUtils.backgroundProcess(tradeReconstructionInputPath, repository, tradeReconstructionArchivePath, tradeReconstructionOutputPath, sortedfiles,url);

} else {

LOGGER.info("Trade Reconstruction Input file is not available to process");

}

}@Override

public String getTemplateTypeUsingUrl(String templateUrl) {

return repository.getDocAssembleTypeName(templateUrl);

}

public List<String> getToMailIdListForAdmLocAndMailTrigger(String location, String mailTrigger) {

return repository.getToMailIdListForAdmLocAndMailTrigger(location,mailTrigger);

}

@Override

public List<String> getMailGroupsForAdmLocAndMailTrigger(String adminLocation, String s) {

return repository.getMailGroupsForAdmLocAndMailTrigger(adminLocation,s);

}

@Override

public void setSftrFlagForCreateMa(MasterAgreement masterAgreement) {

if (AgreementHelper.checkFamilyIsBilateral(masterAgreement) && AgreementHelper.checkSFTRConditions(masterAgreement) && BNP_ENTITY_LEI.contains(masterAgreement.getBnpLei()) && !WM_AGR_TYPES.contains(masterAgreement.getAgreementType().getId().getId())) {

setSftrFlagYes(masterAgreement,masterAgreement.getCreationUser());

}

}

@Override

public void setSftrFlagUpdatingMA(MasterAgreement masterAgreement, User user) {if (AgreementHelper.checkFamilyIsBilateral(masterAgreement) && AgreementHelper.checkSFTRConditionsWithoutCollFlag(masterAgreement) && BNP_ENTITY_LEI.contains(masterAgreement.getBnpLei()) && !WM_AGR_TYPES.contains(masterAgreement.getAgreementType().getId().getId())) {

setSftrFlagYes(masterAgreement,user.getId());

}

}

public void setSftrFlagYes(MasterAgreement masterAgreement,String user) {

if(!repository.findSftrFlagAdded(masterAgreement,new Long(122))) {

SpecialClauseWithoutDetails specialClauseWithoutDetails = new SpecialClauseWithoutDetails(masterAgreement, new Long(122), true);

repository.save(specialClauseWithoutDetails);

repository.flush();

masterAgreement.setSftrFlagSetOnce(true);

List<Object[]> bpCpEntities = repository.fetchExistingEntitiesInSftrLookup(masterAgreement.getBnpParibasEntity().getId(), masterAgreement.getCounterpartyEntity().getId());

if (bpCpEntities != null && bpCpEntities.size() > 0) {

for (Object[] bpCpentity : bpCpEntities) {

SftrLookup sftrLookup = new SftrLookup();sftrLookup.setBnpEntityId(Long.valueOf(bpCpentity[0].toString()));

sftrLookup.setCpEntityId(Long.valueOf(bpCpentity[1].toString()));

sftrLookup.setSpecialClauseValue(new Character('Y'));

sftrLookup.setUserId(user);

repository.save(sftrLookup);

repository.flush();

}

}

}

}

@Override

public void setSftrFlagForUpdateCollateralMa(MasterAgreement masterAgreement, User user) {

if (AgreementHelper.checkFamilyIsBilateral(masterAgreement) && masterAgreement.getAgreementType().isDerivatives() && BNP_ENTITY_LEI.contains(masterAgreement.getBnpLei()) && !WM_AGR_TYPES.contains(masterAgreement.getAgreementType().getId().getId())) {

setSftrFlagYes(masterAgreement, user.getId());

}

}

@Override

public List operationsForMasterAgreement(List<Long> maList) {

Map<Integer, List<Amendment>> docCategoryWithAmendment = new HashMap<>();

for (Long ma : maList) {

MasterAgreement masterAgreement = MasterAgreement.get(repository,ma);

Amendment amendment = masterAgreement.getLastAmendment();

int docCategory = amendment != null ? amendment.getMasterAgreement().getAgreementType().getDocCategory().getId() : 0;

if (!docCategoryWithAmendment.containsKey(docCategory)) {

docCategoryWithAmendment.put(docCategory, new ArrayList());

}

docCategoryWithAmendment.get(docCategory).add(amendment);

}

boolean operationAvailableOrNot = true;

Set<Operation> result = new HashSet<Operation>();

List<String> listOfChangeStatusClass = Arrays.asList("TerminateMasterAgreementOperation",

"DormantMasterAgreementOperation","UnTerminateMasterAgreementOperation","UnDormantMasterAgreementOperation",

"UnExecuteMasterAgreementOperation");

List<Operation> updatedList = new ArrayList();

for (Operation op : getOperations()) {

if (listOfChangeStatusClass.contains(op.getClass().getSimpleName())) {

updatedList.add(op);

}

}

int addToResult = 0;

int sizeOfDocCategory = docCategoryWithAmendment.size();

for (Map.Entry<Integer, List<Amendment>> map : docCategoryWithAmendment.entrySet()) {

addToResult++;List<UserRolePermissions> userPermissions = repository.fetchUserPemissions(auditor.getUser().getRole().getId(), map.getKey());

for (Iterator iterator = updatedList.iterator(); iterator.hasNext(); ) {

Operation operation = (Operation) iterator.next();

if (listOfChangeStatusClass.contains(operation.getClass().getSimpleName())) {

operationAvailableOrNot = true;

for (Iterator itr = userPermissions.iterator(); itr.hasNext(); ) {

UserRolePermissions userRolePermissions = (UserRolePermissions) itr.next();

for (PermissionActions permissionActions : userRolePermissions.getPermission().getPermissionActionsSet()) {

if (permissionActions.getAction().equalsIgnoreCase(operation.getClass().getSimpleName()) && userRolePermissions.isPermissionReadWrite()) {

for (Amendment a : map.getValue()) {

if (!operation.isAvailable(a)) {

operationAvailableOrNot = false;

break;

}

}

if (operationAvailableOrNot) {if ("LEGAL_DATA_INTEGRITY".equalsIgnoreCase(auditor.getUser().getRole().getId())) {

if (!PERMISSIONS_LDI.contains(permissionActions.getAction()))

result.add(operation);

} else {

result.add(operation);

}

}

break;

}

}

}

}

}

updatedList.retainAll(result);

if (addToResult != sizeOfDocCategory) {

result.clear();

}

}

return new ArrayList(result);

}

public void updateAuditHistory(String maIds,String status,String action) {

List<Long> maList = new ArrayList<>();

if (maIds.length() > 0) {

maList = Arrays.asList(maIds.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());

}

repository.updateMasterAgreementAuditHistoryForStatusChange(maList, "UPDATE_AUD_HIST_MULTI_ACTION_MA",status,action);

}

@Override

public Operation getOperation(Class operationClass) {

return findOperation(operationClass);

}@Override

public String getDocAssembleApiKey() {

return repository.getDocAssembleApiKey();

}

@Override

public void sendMailPBMNA() throws IOException {

if(!checkIfJobIsExeOnAnyNode(SEND_MAIL_PBMNA_JOB_ID)) {

makeJobExecutionEntry(node, SEND_MAIL_PBMNA_JOB_ID, SEND_MAIL_PBMNA_JOB_NAME, SEND_MAIL_PBMNA_JOB_DESC);

final String subject = messageSource.getMessage("mail.PBMNA.group.subject", new Object[]{}, null, null);

repository.mergeDataToPBMNAAndPBA();

List<Object[]> pbmnaList = repository.getDataForPBMNAWithoutNetting();

List<Object[]> pbaList = repository.getDataForPBAWithoutNetting();

List<Object[]> isdaList = repository.getDataForISDAWithoutNetting();

Map listValues = new HashMap();

listValues.put("pbmnaList", pbmnaList);

listValues.put("pbaList", pbaList);

listValues.put("isdaList", isdaList);

String body = SimpleTextMessageBuilder.parseMessage("PBMNATable.vm", listValues);

File pbmnaFile = createReportFile(pbmnaList,PBMNA_REPORT,PBMNA_LIST);

File pbaFile = createReportFile(pbaList,PBA_REPORT,PBA_LIST);

File isdaFile = createReportFile(isdaList,ISDA_REPORT,ISDA_LIST);

MailService mailService = applicationContext.getBean(MailService.class);

mailService.sendMailForPbmnaAndPbaReport(pbmnaFile,pbaFile,isdaFile,subject,body);

}

}

@Override

public void sendMailSSNDAForIsda() throws IOException{

if(!checkIfJobIsExeOnAnyNode(SSNDA_MAIL_ISDA_JOB_ID)) {

makeJobExecutionEntry(node, SSNDA_MAIL_ISDA_JOB_ID, SSNDA_MAIL_ISDA_JOB_NAME, SSNDA_MAIL_ISDA_JOB_DESC);

final String subject = messageSource.getMessage("mail.ssndaIsda.subject", new Object[]{}, null, null);

final String body = messageSource.getMessage("mail.ssndaIsda.body",new Object[]{},null,null);

List<Object[]> tab1List = repository.getIsdaForSSNDAReportTab1();

List<Object[]> tab2List = repository.getIsdaForSSNDAReportTab2();

File pbmnaFile = createReportFileWithTwoTabs(tab1List,tab2List,NEW_ISDA, DOCUMENTS_UPDATE_ON_ISDA

,SSNDA_ISDA_REPORT_TAB1_COLUMNS,SSNDA_ISDA_REPORT_TAB2_COLUMNS);MailService mailService = applicationContext.getBean(MailService.class);

List<String> toList = repository.getMailingListForRussianReporting(ALL, SSNDA_ISDA_REPORT,"SSNDA ISDA Report toList");

List<String> ccList = repository.getMailingListForRussianReporting(ALL, SSNDA_ISDA_REPORT,"SSNDA ISDA Report ccList");

mailService.sendMailWithAttachment(pbmnaFile,"ISDA_REPORT.xlsx",subject,body,

toList.stream().collect(Collectors.joining(",")),null,ccList.stream().collect(Collectors.joining(",")));

}

}

//jira 12364

@Override

public void sendMailWeeklyForMSFTA() throws IOException{

if(!checkIfJobIsExeOnAnyNode(SEND_MAIL_MSFTA_JOB_ID)) {

LOGGER.info("Start of send weekly report for MSFTA");

makeJobExecutionEntry(node, SEND_MAIL_MSFTA_JOB_ID, SEND_MAIL_MSFTA_JOB_NAME, SEND_MAIL_MSFTA_JOB_DESC);

final String subject = messageSource.getMessage("mail.MSFTA.subject", new Object[]{}, null, null);

final String body = messageSource.getMessage("mail.MSFTA.body",new Object[]{},null,null);

try {List<Object[]> msftaDataList = repository.getMSFTAWeeklyReport();

if(!msftaDataList.isEmpty()){

File msftaFile = createReportFile(msftaDataList,MSFTA_REPORT, MSFTA_REPORT_COLUMNS);

MailService mailService = applicationContext.getBean(MailService.class);

mailService.sendWeeklyMailForMSFTAReport(msftaFile,subject,body);

}else {

String [] toLists = repository.getMailingListForRussianReporting("NEW YORK", "Weekly MSFTAs created or executed", "Paralegal").toArray(new String[0]);

StringBuilder mailbody = new StringBuilder();

mailbody.append("Hi All,\n\n");

mailbody.append("No records were found for MSFTA agreements for the New York location for this week. \n");

mailbody.append("As a result, no reports have been attached to this email.\n\n");

mailbody.append("This is an automated mail. In case of any issues, please contact GLOBAL CIB BEAGLE SUPPORT: global_cib_beagle_support@bnpparibas.com.\n" +

"\n" +

"Regards,\n" +

"Beagle Team\n");

MailService mailService = applicationContext.getBean(MailService.class);

mailService.sendEmailForManageParameter(Arrays.asList(toLists),Collections.emptyList(),subject,mailbody.toString());

}

} catch (Exception e) {

LOGGER.error("Error while sending mail for MSFTA Weekly report", e);

}

}

}

private File createReportFileWithTwoTabs(List<Object[]> listOfValues,List<Object[]> listOfValues2, String fileName,String fileName2,List<String> columnNameList,List<String> columnNameList2) throws IOException {

File file = new File(fileName+FILE_FORMAT_XLSX);

XSSFWorkbook workbook = new XSSFWorkbook();

XSSFSheet sheet = workbook.createSheet(fileName);

XSSFSheet sheet2 = workbook.createSheet(fileName2);

XSSFRow row = sheet.createRow(0);

XSSFRow row2 = sheet2.createRow(0);

createSheetAndSetData(listOfValues, columnNameList, workbook, row, sheet);

createSheetAndSetData(listOfValues2, columnNameList2, workbook, row2, sheet2);

try(FileOutputStream out = new FileOutputStream(file)) {

sheet.autoSizeColumn(0);

sheet.autoSizeColumn(1);

sheet.autoSizeColumn(2);

sheet.autoSizeColumn(3);

sheet.autoSizeColumn(4);

sheet.autoSizeColumn(5);

sheet.autoSizeColumn(6);

sheet.autoSizeColumn(7);

sheet.autoSizeColumn(8);

sheet.autoSizeColumn(9);

sheet.autoSizeColumn(10);

sheet.autoSizeColumn(11);

sheet.autoSizeColumn(12);

sheet.autoSizeColumn(13);

sheet.autoSizeColumn(14);

sheet.autoSizeColumn(15);

sheet2.autoSizeColumn(0);

sheet2.autoSizeColumn(1);

sheet2.autoSizeColumn(2);

sheet2.autoSizeColumn(3);

sheet2.autoSizeColumn(4);

sheet2.autoSizeColumn(5);

sheet2.autoSizeColumn(6);

sheet2.autoSizeColumn(7);

sheet2.autoSizeColumn(8);

sheet2.autoSizeColumn(9);

sheet2.autoSizeColumn(10);

sheet2.autoSizeColumn(11);

sheet2.autoSizeColumn(12);

sheet2.autoSizeColumn(13);

sheet2.autoSizeColumn(14);

sheet2.autoSizeColumn(15);

workbook.write(out);

} catch (Exception e) {

LOGGER.error("Exception in "+ fileName +" File Creation ", e);

}finally {

if(null != workbook) {workbook.close();

}

}

return file;

}

private void createSheetAndSetData(List<Object[]> listOfValues, List<String> columnNameList, XSSFWorkbook workbook, XSSFRow row, XSSFSheet sheet) {

//Create a new font and alter it.

XSSFFont font = workbook.createFont();

font.setFontHeightInPoints((short) 12);

font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());

//Set font into style

XSSFCellStyle style = workbook.createCellStyle();

style.setFont(font);

int cellNum=0;

for(String cellName : columnNameList){

// Create a cell with a value and set style to it.

XSSFCell cell= row.createCell(cellNum);

cell.setCellValue(cellName);

cell.setCellStyle(style);

cellNum++;

}

int rownum = 1;

for (Object[] value : listOfValues) {

row = sheet.createRow(rownum++);

int cellVal = 0;

for (int cellnum = 0; cellnum <= value.length-1; cellnum++) {

XSSFCell dataCell = row.createCell(cellnum);

if(value[cellnum]!=null){dataCell.setCellValue(String.valueOf(value[cellnum]));

}

cellVal++;

}

}

try(FileOutputStream out = new FileOutputStream(file)) {

sheet.autoSizeColumn(0);

sheet.autoSizeColumn(1);

sheet.autoSizeColumn(2);

sheet.autoSizeColumn(3);

sheet.autoSizeColumn(4);

sheet.autoSizeColumn(5);

sheet.autoSizeColumn(6);

sheet.autoSizeColumn(7);

workbook.write(out);

} catch (Exception e) {

LOGGER.error("Exception in "+ fileName +" File Creation ", e);

}finally {

if(null != workbook) {

workbook.close();

}

}

return file;

}

@Override

public void sendMailSSNDA() throws IOException {

if(!checkIfJobIsExeOnAnyNode(SEND_MAIL_SSNDA_JOB_ID)) {

makeJobExecutionEntry(node, SEND_MAIL_SSNDA_JOB_ID, SEND_MAIL_SSNDA_JOB_NAME, SEND_MAIL_SSNDA_JOB_DESC);

try{

final String subject = messageSource.getMessage("mail.ssnda.group.subject", new Object[]{}, null, null);final String body = messageSource.getMessage("mail.ssnda.group.body", new Object[]{}, null, null);

String firstDateOfPreviousMonth = firstDateOfPreviousMonth();

String lastDateofPreviousMonth = lastDateofPreviousMonth();

List<Object[]> ssndaList = repository.getDataForSSNDAWithoutOverWritten(firstDateOfPreviousMonth,lastDateofPreviousMonth);

if(ssndaList.size()>0){

File ssndaFile = createReportFile(ssndaList,SSNDA_REPORT,SSNDA_LIST);

MailService mailService = applicationContext.getBean(MailService.class);

mailService.sendMailForSSNDAReport(ssndaFile,subject,body);

}

}catch (Exception e){

System.out.println("Exception "+e);

}

}

}

@Override

public MasterAgreement setExecutionDataForBulkUpload(MasterAgreement masterAgreement, AgreementUploadData agreementUploadData){

masterAgreement.setSchedule(agreementUploadData.getSchedule().equalsIgnoreCase("Standard")?"S":"N");

masterAgreement.setCapacityAndAuthority(agreementUploadData.getCapAndAuth());

masterAgreement.setTerminationCurrency(Currency.find(repository,agreementUploadData.getTermCurrency()));

masterAgreement.setSigningAuthority(agreementUploadData.getSigningAuthority().equalsIgnoreCase("Yes")?"Y":agreementUploadData.getSigningAuthority().equalsIgnoreCase("No")?"N":"O");

masterAgreement.setBnpParibasCreditEventUponMerger(agreementUploadData.getBnpCreditEventUponMerger());

masterAgreement.setCounterpartyCreditEventUponMerger(agreementUploadData.getCpCreditEventUponMerger());

masterAgreement.setBnpParibasAutomaticTermination(agreementUploadData.getBnpAutomaticTermination());

masterAgreement.setCounterpartyAutomaticTermination(agreementUploadData.getCpAutomaticTermination());

if(agreementUploadData.getBnpCrossDefaultType().equalsIgnoreCase(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION.getDescription()))

{

masterAgreement.getOrCreateBnpParibasCrossDefault().setAllParameters(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION, null, null

, "", BeagleStringUtils.isY(agreementUploadData.getBnpCrossDefaultType()), null, false, null, null, null,

false, false, null, false, AffiliateType.AFFILIATE_TYPE_ANY,

null, null, "");

}

if(agreementUploadData.getCpCrossDefaultType().equalsIgnoreCase(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION.getDescription()))

{

masterAgreement.getOrCreateCounterpartyCrossDefault().setAllParameters(CrossDefaultType.NO_CROSS_DEFAULT_PROVISION, null, null

, "", BeagleStringUtils.isY(agreementUploadData.getCpCrossDefaultType()), null, false, null, null, null,

false, false, null, false, AffiliateType.AFFILIATE_TYPE_ANY,

null, null, "");

}

return masterAgreement;

}

@Override

public Set<EMIRSpecialClauseJoin> copyEMIRPRDRClauseFormSourceMa(Set<EMIRSpecialClauseJoin> sourceEmirSpecialClauseJoins, MasterAgreement masterAgreement, Date sourceAgrDate) {

Set<EMIRSpecialClauseJoin> targetEmirSpecialClauseJoins = new HashSet<>();

boolean emirMaClauseAvailable = false;

if(masterAgreement.getAgreementType().isNoMasterPRDRAgreement()) {

targetEmirSpecialClauseJoins.add(copyEMIRPRDRClauseForNMPRDRAgr(masterAgreement,sourceAgrDate));

} else if(null != sourceEmirSpecialClauseJoins && sourceEmirSpecialClauseJoins.size() > 0) {

for(EMIRSpecialClauseJoin emirSpecialClause : sourceEmirSpecialClauseJoins) {

if(emirSpecialClause.getId().getJoinedId().equals(Long.valueOf(14))) {

emirMaClauseAvailable = true;

EMIRSpecialClauseJoin emirSpecialClauseJoin = new EMIRSpecialClauseJoin(masterAgreement, (Long) emirSpecialClause.getId().getJoinedId(),emirSpecialClause.getSpecialClauseValue(), emirSpecialClause.getType());

if(null != emirSpecialClause.getSignDate()) {

emirSpecialClauseJoin.setSignDate(emirSpecialClause.getSignDate());

}

targetEmirSpecialClauseJoins.add(emirSpecialClauseJoin);

}

}

if(!emirMaClauseAvailable) {

EMIRSpecialClauseJoin emirSpecialClauseJoin = new EMIRSpecialClauseJoin(masterAgreement,Long.valueOf(14),Long.valueOf(2), EMIRSpecialClauseGroup.ISDA_CLAUSE.getId());

targetEmirSpecialClauseJoins.add(emirSpecialClauseJoin);

}

} else {

EMIRSpecialClauseJoin emirSpecialClauseJoin = new EMIRSpecialClauseJoin(masterAgreement,Long.valueOf(14),Long.valueOf(2), EMIRSpecialClauseGroup.ISDA_CLAUSE.getId());

targetEmirSpecialClauseJoins.add(emirSpecialClauseJoin);

}

return targetEmirSpecialClauseJoins;

}

private EMIRSpecialClauseJoin copyEMIRPRDRClauseForNMPRDRAgr(MasterAgreement masterAgreement, Date sourceAgrDate) {

EMIRSpecialClauseJoin emirSpecialClauseJoin = new EMIRSpecialClauseJoin(masterAgreement,Long.valueOf(14),Long.valueOf(1), EMIRSpecialClauseGroup.ISDA_CLAUSE.getId());

if(null != sourceAgrDate) {

emirSpecialClauseJoin.setSignDate(sourceAgrDate);

} else {

emirSpecialClauseJoin.setSignDate(new Date());

}

return emirSpecialClauseJoin;

}public boolean isSSNDAEnitityPresentInMA(MasterAgreement ma){

return repository.isRestrictedEntityPresentInMA(ma.getId());

}

/*    This method is used for PBA Key Terms Tab*/

public void copyPBAKeyTermsDataFromSourceMaToTargetMa(MasterAgreement targetMA, Long sourceMaId){

MasterAgreement sourceMa = (MasterAgreement) repository.getSession().get(MasterAgreement.class, Long.valueOf(sourceMaId));

if(sourceMa.getAgreementType().isPBA() && isSSNDAEnitityPresentInMA(sourceMa) && targetMA.getAgreementType().isPBA() && isSSNDAEnitityPresentInMA(targetMA)){

copyPbaAffiliateData(sourceMa,targetMA);

copyPbaMarginMaintenanceData(sourceMa,targetMA);

copyPbaKeyTermMiscellaneous(sourceMa,targetMA);

copyPbaEventOfDefault(sourceMa,targetMA);

copyPbaFailureToPayPostMargin(sourceMa,targetMA);

copyPbaInternalCrossDefault(sourceMa,targetMA);

copyPbaExternalCrossDefault(sourceMa,targetMA);

copyPbaFishOrCutBait(sourceMa,targetMA);

}

}

/*This method is used for LUA Key Terms Tab Start*/public void copyLUAKeyTermsDataFromSourceMaToTargetMa(MasterAgreement targetMA, Long sourceMaId){

MasterAgreement sourceMa = (MasterAgreement) repository.getSession().get(MasterAgreement.class, Long.valueOf(sourceMaId));

if(sourceMa.getAgreementType().isLUA() && isSSNDAEnitityPresentInMA(sourceMa) && targetMA.getAgreementType().isLUA() && isSSNDAEnitityPresentInMA(targetMA)){

copyLUACoveredProducts(sourceMa,targetMA);

copyLuaKeyTermMiscellaneous(sourceMa,targetMA);

copyLuaScopeOfCommitmentModifMethod(sourceMa,targetMA);

copyLuaLiquidityCoverageRatio(sourceMa,targetMA);

copyLuaEventsOfDefaultSection(sourceMa,targetMA);

copyLuaLockUpTerminationEvent(sourceMa,targetMA);

copyLuaNavTriggerTermPeriod(sourceMa,targetMA);

copyLuaNavTriggerFloor(sourceMa,targetMA);

copyLuaPreNoticeFinancingCap(sourceMa,targetMA);

copyLuaPostNoticeFinancingCap(sourceMa,targetMA);

copyLuaMarginMaintenance(sourceMa,targetMA);

copyLuaFishOrCutBait(sourceMa,targetMA);

}

}public void copyLUACoveredProducts(MasterAgreement sourceMa, MasterAgreement targetMA){

for (LuaCoveredProduct coveredProduct : sourceMa.getLuaCoveredProduct()) {

LuaCoveredProduct luaCoveredProduct = new LuaCoveredProduct(targetMA, LoadLuaKeyTermEnum.getLuaKeyTermEnumByName((coveredProduct.getProductType())));

luaCoveredProduct.setProductType(coveredProduct.getProductType());

targetMA.setLuaCoveredProduct(luaCoveredProduct);

}

}

public void copyLuaKeyTermMiscellaneous(MasterAgreement sourceMa, MasterAgreement targetMA){

LuaKeyTermMiscellaneousSections luaKeyTermMiscellaneousSections = new LuaKeyTermMiscellaneousSections();

if(sourceMa.getLuaKeyTermMiscellaneousSections()!=null){

luaKeyTermMiscellaneousSections.setNoticePeriodDays(sourceMa.getLuaKeyTermMiscellaneousSections().getNoticePeriodDays());

luaKeyTermMiscellaneousSections.setNoticePeriodDayType(sourceMa.getLuaKeyTermMiscellaneousSections().getNoticePeriodDayType());

luaKeyTermMiscellaneousSections.setLeadingToPriceChange(sourceMa.getLuaKeyTermMiscellaneousSections().getLeadingToPriceChange());

luaKeyTermMiscellaneousSections.setLeadingToTermination(sourceMa.getLuaKeyTermMiscellaneousSections().getLeadingToTermination());

luaKeyTermMiscellaneousSections.setMarginexcess_Return_Collateral_Type(sourceMa.getLuaKeyTermMiscellaneousSections().getMarginexcess_Return_Collateral_Type());

luaKeyTermMiscellaneousSections.setComments(sourceMa.getLuaKeyTermMiscellaneousSections().getComments());

luaKeyTermMiscellaneousSections.setMasterAgreement(targetMA);

targetMA.setLuaKeyTermMiscellaneousSections(luaKeyTermMiscellaneousSections);

}

}

public void copyLuaScopeOfCommitmentModifMethod(MasterAgreement sourceMa, MasterAgreement targetMA){

for (LuaScopeOfCommitment modificationMethod : sourceMa.getLuaScopeOfCommitment()) {

LuaScopeOfCommitment luaScopeOfCommitment = new LuaScopeOfCommitment(targetMA,LoadLuaKeyTermEnum.getLuaKeyTermEnumByName((modificationMethod.getModificationMethd())));

luaScopeOfCommitment.setModificationMethd(modificationMethod.getModificationMethd());

targetMA.setLuaScopeOfCommitment(luaScopeOfCommitment);

}

}

public void copyLuaLiquidityCoverageRatio(MasterAgreement sourceMa, MasterAgreement targetMA){

for (LuaLiquidityCoverageRatio luaLiquidityCoverageRatio : sourceMa.getLuaLiquidityCoverageRatio()) {

LuaLiquidityCoverageRatio lcrMethod = new LuaLiquidityCoverageRatio(targetMA, LoadLuaKeyTermEnum.getLuaKeyTermEnumByName((luaLiquidityCoverageRatio.getLcrMethod())));

lcrMethod.setLcrMethod(luaLiquidityCoverageRatio.getLcrMethod());

targetMA.setLuaLiquidityCoverageRatio(lcrMethod);

}

}

public void copyLuaEventsOfDefaultSection(MasterAgreement sourceMa, MasterAgreement targetMA){

for(LuaEventOfDefaultSection luaEventOfDefult : sourceMa.getLuaEventOfDefaultSection()){LuaEventOfDefaultSection luaEventOfDefaultSection = new LuaEventOfDefaultSection();

luaEventOfDefaultSection.setRowId(luaEventOfDefult.getRowId());

luaEventOfDefaultSection.setClientApplicability(luaEventOfDefult.getClientApplicability());

luaEventOfDefaultSection.setDefaultEventType(luaEventOfDefult.getDefaultEventType());

luaEventOfDefaultSection.setGracePeriodDayRef(luaEventOfDefult.getGracePeriodDayRef());

luaEventOfDefaultSection.setGracePeriodDay(luaEventOfDefult.getGracePeriodDay());

luaEventOfDefaultSection.setMasterAgreement(targetMA);

for(LuaEventOfDefaultConditions eventOfDefaultConditions : luaEventOfDefult.getLuaEventOfDefaultCondition()) {

LuaEventOfDefaultConditions luaEventOfDefaultConditions = new LuaEventOfDefaultConditions();

luaEventOfDefaultConditions.setLuaEventOfDefaultSection(luaEventOfDefaultSection);

luaEventOfDefaultConditions.setLuaKeyTermEnum(eventOfDefaultConditions.getLuaKeyTermEnum());

luaEventOfDefaultSection.setLuaEventOfDefaultCondition(luaEventOfDefaultConditions);

}

targetMA.setLuaEventOfDefaultSection(luaEventOfDefaultSection);

}

}

public void copyLuaLockUpTerminationEvent(MasterAgreement sourceMa, MasterAgreement targetMA){

for(LuaLockUpTerminationEvent lockUpTerminationEvent : sourceMa.getLuaLockUpTerminationEvent()){

AgreementKey key = new AgreementKey(targetMA,lockUpTerminationEvent.getId().getJoinedId());

LuaLockUpTerminationEvent luaLockUpTerminationEvent = new LuaLockUpTerminationEvent(key);

luaLockUpTerminationEvent.setEventType(lockUpTerminationEvent.getEventType());

luaLockUpTerminationEvent.setCurePeriodType(lockUpTerminationEvent.getCurePeriodType());

luaLockUpTerminationEvent.setCurePeriod(lockUpTerminationEvent.getCurePeriod());

targetMA.setLuaLockUpTerminationEvent(luaLockUpTerminationEvent);

}

}

public void copyLuaNavTriggerTermPeriod(MasterAgreement sourceMa, MasterAgreement targetMA){for(LuaNavTriggerPeriod navTriggerPeriod : sourceMa.getLuaNavTriggerPeriod()){

AgreementKey key = new AgreementKey(targetMA,navTriggerPeriod.getId().getJoinedId());

LuaNavTriggerPeriod luaNavTriggerTermPeriod = new LuaNavTriggerPeriod(key);

luaNavTriggerTermPeriod.setAffectedParty(navTriggerPeriod.getAffectedParty());

luaNavTriggerTermPeriod.setDeclinePercentage(navTriggerPeriod.getDeclinePercentage());

luaNavTriggerTermPeriod.setDeclineType(navTriggerPeriod.getDeclineType());

luaNavTriggerTermPeriod.setNavTriggerType(navTriggerPeriod.getNavTriggerType());

luaNavTriggerTermPeriod.setTriggerDeterminationDate(navTriggerPeriod.getTriggerDeterminationDate());

luaNavTriggerTermPeriod.setTriggerDeterminationDateLookBack(navTriggerPeriod.getTriggerDeterminationDateLookBack());

luaNavTriggerTermPeriod.setNavTermComplex(navTriggerPeriod.getNavTermComplex());

targetMA.setLuaNavTriggerPeriod(luaNavTriggerTermPeriod);

}

}

public void copyLuaNavTriggerFloor(MasterAgreement sourceMa, MasterAgreement targetMA){for(LuaNavTriggerFloor navTriggerFloor: sourceMa.getLuaNavTriggerFloor()){

AgreementKey key = new AgreementKey(targetMA,navTriggerFloor.getId().getJoinedId());

LuaNavTriggerFloor luaNavTriggerFloor = new LuaNavTriggerFloor(key);

luaNavTriggerFloor.setAffectedParty(navTriggerFloor.getAffectedParty());

luaNavTriggerFloor.setNavComparision(navTriggerFloor.getNavComparision());

luaNavTriggerFloor.setFloorAmount(navTriggerFloor.getFloorAmount());

luaNavTriggerFloor.setFloorAmountCurrency(navTriggerFloor.getFloorAmountCurrency());

luaNavTriggerFloor.setFloorAmountMethod(navTriggerFloor.getFloorAmountMethod());

luaNavTriggerFloor.setFloorDeclineType(navTriggerFloor.getFloorDeclineType());

luaNavTriggerFloor.setTriggerDeterminationDate(navTriggerFloor.getTriggerDeterminationDate());

luaNavTriggerFloor.setTriggerDeterminationDateLookBack(navTriggerFloor.getTriggerDeterminationDateLookBack());

luaNavTriggerFloor.setNavTermComplex(navTriggerFloor.getNavTermComplex());

targetMA.setLuaNavTriggerFloor(luaNavTriggerFloor);

}

}

public void copyLuaPreNoticeFinancingCap(MasterAgreement sourceMa, MasterAgreement targetMA){

for(LuaPrePostNoticeFinancingCap prePostNoticeFinancingCap: sourceMa.getLuaPreNoticeFinancingCap()){

LuaPrePostNoticeFinancingCap luaPrePostNoticeFinancingCap = new LuaPrePostNoticeFinancingCap(targetMA,prePostNoticeFinancingCap.getRowId(),"PRE");

luaPrePostNoticeFinancingCap.setLimitType(prePostNoticeFinancingCap.getLimitType());

luaPrePostNoticeFinancingCap.setLimitAmountRelation(prePostNoticeFinancingCap.getLimitAmountRelation());

luaPrePostNoticeFinancingCap.setLimitAmountMethod(prePostNoticeFinancingCap.getLimitAmountMethod());

luaPrePostNoticeFinancingCap.setLookBackDays(prePostNoticeFinancingCap.getLookBackDays());

luaPrePostNoticeFinancingCap.setLimitAmountPercentage(prePostNoticeFinancingCap.getLimitAmountPercentage());

luaPrePostNoticeFinancingCap.setLimitAmountMaxCap(prePostNoticeFinancingCap.getLimitAmountMaxCap());luaPrePostNoticeFinancingCap.setLookBackDayType(prePostNoticeFinancingCap.getLookBackDayType());

luaPrePostNoticeFinancingCap.setLimitAmountMaxCapCurrency(prePostNoticeFinancingCap.getLimitAmountMaxCapCurrency());

luaPrePostNoticeFinancingCap.setFinancingCapComplex(prePostNoticeFinancingCap.getFinancingCapComplex());

targetMA.setLuaPrePostNoticeFinancingCap(luaPrePostNoticeFinancingCap);

}

}

public void copyLuaPostNoticeFinancingCap(MasterAgreement sourceMa, MasterAgreement targetMA){

for(LuaPrePostNoticeFinancingCap prePostNoticeFinancingCap: sourceMa.getLuaPostNoticeFinancingCap()){

LuaPrePostNoticeFinancingCap luaPrePostNoticeFinancingCap = new LuaPrePostNoticeFinancingCap(targetMA,prePostNoticeFinancingCap.getRowId(),"POST");

luaPrePostNoticeFinancingCap.setLimitType(prePostNoticeFinancingCap.getLimitType());

luaPrePostNoticeFinancingCap.setLimitAmountRelation(prePostNoticeFinancingCap.getLimitAmountRelation());

luaPrePostNoticeFinancingCap.setLimitAmountMethod(prePostNoticeFinancingCap.getLimitAmountMethod());

luaPrePostNoticeFinancingCap.setLookBackDays(prePostNoticeFinancingCap.getLookBackDays());

luaPrePostNoticeFinancingCap.setLimitAmountPercentage(prePostNoticeFinancingCap.getLimitAmountPercentage());

luaPrePostNoticeFinancingCap.setLimitAmountMaxCap(prePostNoticeFinancingCap.getLimitAmountMaxCap());

luaPrePostNoticeFinancingCap.setLookBackDayType(prePostNoticeFinancingCap.getLookBackDayType());

luaPrePostNoticeFinancingCap.setLimitAmountMaxCapCurrency(prePostNoticeFinancingCap.getLimitAmountMaxCapCurrency());

luaPrePostNoticeFinancingCap.setFinancingCapComplex(prePostNoticeFinancingCap.getFinancingCapComplex());

targetMA.setLuaPrePostNoticeFinancingCap(luaPrePostNoticeFinancingCap);

}

}

public void copyLuaMarginMaintenance(MasterAgreement sourceMa, MasterAgreement targetMA){

for(MarginMaintenance marginMaintenance : sourceMa.getLuaMarginMaintenance()){

AgreementKey key = new AgreementKey(targetMA,marginMaintenance.getId().getJoinedId());

MarginMaintenance luaMarginMaintenance = new MarginMaintenance(key);

luaMarginMaintenance.setMarginNotificationTimePeriod(marginMaintenance.getMarginNotificationTimePeriod());

luaMarginMaintenance.setMarginNotificationTimeLocation(marginMaintenance.getMarginNotificationTimeLocation());

luaMarginMaintenance.setMarginNotificationTime(marginMaintenance.getMarginNotificationTime());

luaMarginMaintenance.setDayType(marginMaintenance.getDayType());

luaMarginMaintenance.setMarginTranferTimePeriod(marginMaintenance.getMarginTranferTimePeriod());

luaMarginMaintenance.setMarginTransferTimeLocation(marginMaintenance.getMarginTransferTimeLocation());

luaMarginMaintenance.setMarginTranferTiming(marginMaintenance.getMarginTranferTiming());

luaMarginMaintenance.setMarginTransferTime(marginMaintenance.getMarginTransferTime());

luaMarginMaintenance.setMarginDelieveryPerPba(marginMaintenance.getMarginDelieveryPerPba());

targetMA.setLuaMarginMaintenance(luaMarginMaintenance);

}

}

public void copyLuaFishOrCutBait(MasterAgreement sourceMa, MasterAgreement targetMA){

for(FishOrCutBait fishOrCutBait : sourceMa.getLuaFishOrCutBait()){

AgreementKey key = new AgreementKey(targetMA,fishOrCutBait.getId().getJoinedId());

FishOrCutBait luaFishOrCutBait = new FishOrCutBait(key);

luaFishOrCutBait.setFocbApplicable(fishOrCutBait.getFocbApplicable());

luaFishOrCutBait.setAllEventsOfDefault(fishOrCutBait.getAllEventsOfDefault());

luaFishOrCutBait.setDeemedWaiverDayType(fishOrCutBait.getDeemedWaiverDayType());

luaFishOrCutBait.setAllTerminationEvents(fishOrCutBait.getAllTerminationEvents());

luaFishOrCutBait.setDeemedWaiverDays(fishOrCutBait.getDeemedWaiverDays());

targetMA.setLuaFishOrCutBait(luaFishOrCutBait);

}

}

/*This method is used for LUA Key Terms Tab End*/

public void copyPbaAffiliateData(MasterAgreement sourceMa, MasterAgreement targetMA){

List<Long> selectedAffiliatesId = new ArrayList<>();for(AffiliatesPbaKeyTerm a : sourceMa.getAffiliatesPbaKeyTermJoins() ){

selectedAffiliatesId.add(a.getPbaKeyTermEnum().getId());

}

if(selectedAffiliatesId!=null && selectedAffiliatesId.size()>0) {

for (Long selectedId : selectedAffiliatesId) {

targetMA.setAffiliatesPbaKeyTermJoins(new AffiliatesPbaKeyTerm(targetMA, LoadPbaKeyTermEnum.getPbaKeyTermEnumById(selectedId)));

}

}

}

public void copyPbaMarginMaintenanceData(MasterAgreement sourceMa, MasterAgreement targetMA){

for(MarginMaintenance marginMaintenance : sourceMa.getMarginMaintenanceList() ){

MarginMaintenance targetMarginMaintenance = new MarginMaintenance();

targetMarginMaintenance.setRowId(marginMaintenance.getRowId());

targetMarginMaintenance.setMarginNotificationTimePeriod(marginMaintenance.getMarginNotificationTimePeriod());

targetMarginMaintenance.setMarginNotificationTimeLocation(marginMaintenance.getMarginNotificationTimeLocation());

targetMarginMaintenance.setMarginNotificationTime(marginMaintenance.getMarginNotificationTime());targetMarginMaintenance.setDayType(marginMaintenance.getDayType());

targetMarginMaintenance.setMarginTranferTimePeriod(marginMaintenance.getMarginTranferTimePeriod());

targetMarginMaintenance.setMarginTransferTimeLocation(marginMaintenance.getMarginTransferTimeLocation());

targetMarginMaintenance.setMarginTranferTiming(marginMaintenance.getMarginTranferTiming());

targetMarginMaintenance.setMarginTransferTime(marginMaintenance.getMarginTransferTime());

targetMarginMaintenance.setMasterAgreement(targetMA);

targetMA.setMarginMaintenanceList(targetMarginMaintenance);

}

}

public void copyPbaKeyTermMiscellaneous(MasterAgreement sourceMa, MasterAgreement targetMA){

PbaKeyTermMiscellaneousSections misc = new PbaKeyTermMiscellaneousSections();

if(sourceMa.getPbaKeyTermMiscellaneousSections()!=null){

misc.setComments(sourceMa.getPbaKeyTermMiscellaneousSections().getComments());

misc.setFinRepayOfCashLoan(sourceMa.getPbaKeyTermMiscellaneousSections().getFinRepayOfCashLoan());

misc.setMarginExcRetFailConseq(sourceMa.getPbaKeyTermMiscellaneousSections().getMarginExcRetFailConseq());

misc.setMarginExcRetObligation(sourceMa.getPbaKeyTermMiscellaneousSections().getMarginExcRetObligation());

misc.setDefRemNotifRequired(sourceMa.getPbaKeyTermMiscellaneousSections().getDefRemNotifRequired());

misc.setDefRemNotifType(sourceMa.getPbaKeyTermMiscellaneousSections().getDefRemNotifType());

misc.setDefRemTrigger(sourceMa.getPbaKeyTermMiscellaneousSections().getDefRemTrigger());

misc.setSetOffRights(sourceMa.getPbaKeyTermMiscellaneousSections().getSetOffRights());

misc.setSetOffNotifType(sourceMa.getPbaKeyTermMiscellaneousSections().getSetOffNotifType());

misc.setPbLiabStd(sourceMa.getPbaKeyTermMiscellaneousSections().getPbLiabStd());

misc.setPbLiabResponseType(sourceMa.getPbaKeyTermMiscellaneousSections().getPbLiabResponseType());

misc.setMasterAgreement(targetMA);

targetMA.setPbaKeyTermMiscellaneousSections(misc);

}

}public void copyPbaEventOfDefault(MasterAgreement sourceMa, MasterAgreement targetMA){

for(EventOfDefault e : sourceMa.getEventsOfDefaultList()){

AgreementKey key = new AgreementKey(targetMA,e.getId().getJoinedId());

EventOfDefault eventOfDefault = new EventOfDefault(key);

eventOfDefault.setBnppApplicability(e.getBnppApplicability());

eventOfDefault.setClientApplicability(e.getClientApplicability());

eventOfDefault.setDefaultEventType(e.getDefaultEventType());

eventOfDefault.setGracePeriodDay(e.getGracePeriodDay());

eventOfDefault.setGracePeriodDayRef(e.getGracePeriodDayRef());

eventOfDefault.setMaterialityQualifier(e.getMaterialityQualifier());

targetMA.setEventsOfDefaultList(eventOfDefault);

}

}

public void copyPbaFailureToPayPostMargin(MasterAgreement sourceMa, MasterAgreement targetMA){

for(FailureToPayPostMargin f : sourceMa.getFailureToPayPostMarginList()){

FailureToPayPostMargin failureToPayPostMargin = new FailureToPayPostMargin();

failureToPayPostMargin.setRowId(f.getRowId());

failureToPayPostMargin.setClientApplicable(f.getClientApplicable());

failureToPayPostMargin.setCureType(f.getCureType());

failureToPayPostMargin.setAdmErrorCurePeriodTimeLoc(f.getAdmErrorCurePeriodTimeLoc());

failureToPayPostMargin.setCureCondition(f.getCureCondition());

failureToPayPostMargin.setAdmErrorCurePeriodTiming(f.getAdmErrorCurePeriodTiming());

failureToPayPostMargin.setCurePeriodDayType(f.getCurePeriodDayType());

failureToPayPostMargin.setMasterAgreement(targetMA);

for(FailureToPayPostMarginEventType failureType : f.getFailureToPayPostMarginEventTypes()){

FailureToPayPostMarginEventType failureToPayPostMarginEventType = new FailureToPayPostMarginEventType();

failureToPayPostMarginEventType.setFailureToPayPostMargin(failureToPayPostMargin);

failureToPayPostMarginEventType.setEventType(failureType.getEventType());

failureToPayPostMargin.setFailureToPayPostMarginEventTypes(failureToPayPostMarginEventType);

}targetMA.setFailureToPayPostMarginList(failureToPayPostMargin);

}

}

public void copyPbaInternalCrossDefault(MasterAgreement sourceMa, MasterAgreement targetMA){

for(InternalCrossDefault i : sourceMa.getInternalCrossDefaults()){

InternalCrossDefault internalCrossDefault = new InternalCrossDefault();

internalCrossDefault.setRowId(i.getRowId());

internalCrossDefault.setBnpApplicability(i.getBnpApplicability());

internalCrossDefault.setClientApplicability(i.getClientApplicability());

internalCrossDefault.setCrossDefaultStandard(i.getCrossDefaultStandard());

internalCrossDefault.setThresholdApplicable(i.getThresholdApplicable());

internalCrossDefault.setMasterAgreement(targetMA);

for(InternalCrossDefaultFailureType icdft : i.getInternalCrossDefaultFailureTypes()){

InternalCrossDefaultFailureType intCrsDefFailureType = new InternalCrossDefaultFailureType();

intCrsDefFailureType.setInternalCrossDefault(internalCrossDefault);

intCrsDefFailureType.setFailureType(icdft.getFailureType());

internalCrossDefault.setInternalCrossDefaultFailureTypes(intCrsDefFailureType);

}

for(InternalCrossDefaultEntityInScope icde : i.getInternalCrossDefaultEntities()){

InternalCrossDefaultEntityInScope intCrsDefEntity = new InternalCrossDefaultEntityInScope();

intCrsDefEntity.setInternalCrossDefault(internalCrossDefault);

intCrsDefEntity.setEntityInScope(icde.getEntityInScope());

internalCrossDefault.setInternalCrossDefaultEntities(intCrsDefEntity);

}

for(InternalCrossDefaultAgreementInScope icdas : i.getInternalCrossDefaultAgreements()){

InternalCrossDefaultAgreementInScope intCrsDefAgr = new InternalCrossDefaultAgreementInScope();

intCrsDefAgr.setInternalCrossDefault(internalCrossDefault);

intCrsDefAgr.setAgreementInScope(icdas.getAgreementInScope());

internalCrossDefault.setInternalCrossDefaultAgreements(intCrsDefAgr);

}

targetMA.setInternalCrossDefaults(internalCrossDefault);

}

}

public void copyPbaExternalCrossDefault(MasterAgreement sourceMa, MasterAgreement targetMA){

for(ExternalCrossDefault e : sourceMa.getExternalCrossDefaults()){

ExternalCrossDefault externalCrossDefault = new ExternalCrossDefault();

externalCrossDefault.setRowId(e.getRowId());

externalCrossDefault.setBnpApplicability(e.getBnpApplicability());

externalCrossDefault.setClientApplicability(e.getClientApplicability());

externalCrossDefault.setCrossDefaultStandard(e.getCrossDefaultStandard());

externalCrossDefault.setThresholdApplicable(e.getThresholdApplicable());

externalCrossDefault.setMasterAgreement(targetMA);

for(ExternalCrossDefaultFailureType ecdft : e.getExternalCrossDefaultFailureTypes()) {

ExternalCrossDefaultFailureType newRowExtCrsDefFailureType = new ExternalCrossDefaultFailureType();

newRowExtCrsDefFailureType.setExternalCrossDefault(externalCrossDefault);

newRowExtCrsDefFailureType.setFailureType(ecdft.getFailureType());

externalCrossDefault.setExternalCrossDefaultFailureTypes(newRowExtCrsDefFailureType);

}targetMA.setExternalCrossDefaults(externalCrossDefault);

}

}

public void copyPbaFishOrCutBait(MasterAgreement sourceMa, MasterAgreement targetMA){

for(FishOrCutBait fishOrCutBait : sourceMa.getFishOrCutBaitList()){

FishOrCutBait focb = new FishOrCutBait();

focb.setRowId(fishOrCutBait.getRowId());

focb.setFocbApplicable(fishOrCutBait.getFocbApplicable());

focb.setAllEventsOfDefault(fishOrCutBait.getAllEventsOfDefault());

focb.setDeemedWaiverDayType(fishOrCutBait.getDeemedWaiverDayType());

focb.setAllTerminationEvents(fishOrCutBait.getAllTerminationEvents());

focb.setDeemedWaiverDays(fishOrCutBait.getDeemedWaiverDays());

focb.setMasterAgreement(targetMA);

targetMA.setFishOrCutBaitList(focb);

}

}

}
