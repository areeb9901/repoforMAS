This is ShowMasterAgreement.java


package com.bnpparibas.beagle.ma.actions;

import com.bnpparibas.beagle.collaterals.model.CollateralControlAgreement;

import com.bnpparibas.beagle.collaterals.model.CollateralData;

import com.bnpparibas.beagle.config.helper.PeCrdsConfigHelper;

import com.bnpparibas.beagle.coveredoffices.model.CoveredOffice;

import com.bnpparibas.beagle.coveredoffices.ui.CoveredOfficeDisplay;

import com.bnpparibas.beagle.documents.CollateralOpsService;

import com.bnpparibas.beagle.documents.DocumentClass;

import com.bnpparibas.beagle.documents.NegotiationService;

import com.bnpparibas.beagle.documents.model.DocumentTypeFilter;

import com.bnpparibas.beagle.groups.job.ui.AuditStatus;

import com.bnpparibas.beagle.groups.model.SubGroup;

import com.bnpparibas.beagle.indexing.Constants;

import com.bnpparibas.beagle.isdacdealinkagecontrol.model.BannerModule;

import com.bnpparibas.beagle.isdacdealinkagecontrol.ui.IsdaCdeaLinkageControlConstants;

import com.bnpparibas.beagle.kernel.actions.BeagleRuntimeException;import com.bnpparibas.beagle.kernel.actions.ToolbarItem;

import com.bnpparibas.beagle.kernel.operations.Operation;

import com.bnpparibas.beagle.kernel.operations.Parameters;

import com.bnpparibas.beagle.kernel.operations.ParametersHolder;

import com.bnpparibas.beagle.kernel.security.UserRolePermissions;

import com.bnpparibas.beagle.kernel.services.FileSystemAccess;

import com.bnpparibas.beagle.kernel.util.*;

import com.bnpparibas.beagle.legaldata.model.Regulator;

import com.bnpparibas.beagle.ma.indexing.MasterAgreementFields;

import com.bnpparibas.beagle.ma.model.*;

import com.bnpparibas.beagle.ma.ui.MasterAgreementForm;

import com.bnpparibas.beagle.ma.ui.MasterAgreementHeaderDisplay;

import com.bnpparibas.beagle.ma.ui.Tabs;

import com.bnpparibas.beagle.ma.ui.XPAAgreementConstants;

import com.bnpparibas.beagle.staticdata.model.*;

import com.bnpparibas.beagle.staticdata.model.entity.Entity;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

import java.util.*;import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;

import static com.bnpparibas.beagle.kernel.util.StrutsUtils.storeInSession;

public class ShowMasterAgreement extends MasterAgreementActionSupport implements CookieAware, ParametersHolder {

public static final String BEAGLE_RECENT_AGREEMENTS_COOKIE_KEY = "beagle.cib.echonet.recent.agreements";

public static final String BEAGLE_RECENT_SEARCHES_COOKIE_KEY = "beagle.cib.echonet.recent.searches";

public static final String CLOSE_OUT_GROUP = "Close Out Group";

public static final String COVERED_AGREEMENTS = "Covered Agreements";

public static final String MASTER_NETTING_AGREEMENT = "Master Netting Agreement";

public static final String CLEARING_PARENT_AGREEMENT = "Clearing Parent Agreement";

private static final List<String> MA_TYPE_DERIVATIVE = Arrays.asList("ISDA (1992)", "ISDA (2002)", "ISDA (1987)");

public static final String PRE_EXECUTION_MODE_KEY = "preExecutionModeFor";

private static final String agreementTypeInitialText = "PB-";

private static final String agreementTypeInitialTextForEFETMNA = "EFET ";

private FileSystemAccess fileSystemAccess;

private String closeOutNettingControl;

private BoundedFifoBufferCookie recentAgreements = new BoundedFifoBufferCookie();

private Map availableActions = new HashMap();

private MasterAgreementHeaderDisplay masterAgreementHeaderDisplay;

private MasterAgreementForm form;

protected boolean executionMode;

protected boolean preExecutionMode;

protected boolean isTabCorrespondenceAccessableToUserAsPerChineseWall;

protected boolean isTabExecutedDocumentationAccessableToUserAsPerChineseWall;

private String chineseWallRestrictionMessage = "The contents of this tab are restricted to \"<i>{0}</i>\" entity users.";

protected boolean russianMailSent;

protected boolean isConsolidatedAuditToBeShown;

private static final List<Long> TAB_ID_LIST = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L);

private String initialTab = "";

private static final String EXCEL = "excel";

private String view;

private String isCorrespondenceAccessible;

private String usersWhoHaveAccessToThisMA;

private static final Long BNP_PARIBAS_SECURITIES_CORP_ENTITY = 5169L;

private long delay = 0;

private Set fields;

private String module;

private BannerModule bannerModule;

private boolean groupTabOlderView;

private String message;

private boolean groupEditJobAlreadySubmitted;

protected boolean readyToSignMode;

private PeCrdsConfigHelper peCrdsConfigHelper;

private boolean amdExeCancelled;

private CollateralOpsService collateralOpsService;

List selectedMAPb = new ArrayList();

public String getMessage() {

return message;

}

public void setMessage(String message) {

this.message = message;

}

public String getAgreementType() {

return this.masterAgreement.getAgreementType().toString();

}

public String getAgreementClass() {

return this.masterAgreement.isPrivate()?"Private":"NotPrivate";}

@Override

protected String executeMasterAgreementAction() {

recentAgreements.addItem(String.valueOf(masterAgreementId));

clearSession();

if (EXCEL.equals(view)) {

return EXCEL;

}

if ( isCategoryRestricted() && amdExeCancelled && (masterAgreement.getLastAmendment().isInNegotiation() || !selectedMAPb.isEmpty())) {

if(masterAgreement.getGroup()!=null){

getSelectedMAPb().stream().forEach(ma -> {

Amendment amd = repository.findMaster((Long) ma);

deleteLatestAmd(amd.getMasterAgreement().getLastAmendment().getType(), amd.getMasterAgreement().removeAmendment(amd.getMasterAgreement().getLastAmendment().getAmendmentId()));

});

}else

deleteLatestAmd(masterAgreement.getLastAmendment().getType(), masterAgreement.removeAmendment(masterAgreement.getLastAmendment().getAmendmentId()));

setAmdExeCancelled(Boolean.FALSE);

}

loadChineseWallDetails();

loadAvailableOperations();

addMessages();

return SUCCESS;

}

@Override

public Parameters getParameters() {

return null;

}protected void loadChineseWallDetails() {

List<Entity> bpChineseWallEntities = (List<Entity>) session.get(Entity.BP_CHINESEWALL_ENTITIES);

Entity bpEntity = masterAgreement.getBnpParibasEntity();

Entity cpEntity = masterAgreement.getCounterpartyEntity();

Entity bpParentEntity = null;

if (!bpEntity.isParent()) {

bpParentEntity = bpEntity.getParent();

}

if (bpEntity.isParent()) {

bpParentEntity = bpEntity;

}

if (cpEntity.isPrivate()) {

if (bpParentEntity != null && bpChineseWallEntities.contains(bpParentEntity)) {

String entityName = bpParentEntity.getLegalName() + " " + bpParentEntity.getCodes();

if (!(bpParentEntity.getUsersWhoHaveAccessToThisEntity().contains(new ChineseWallUser(bpParentEntity, getUser())))) {

isTabCorrespondenceAccessableToUserAsPerChineseWall = false;

isTabExecutedDocumentationAccessableToUserAsPerChineseWall = false;

chineseWallRestrictionMessage = MessageFormat.format(chineseWallRestrictionMessage, entityName);

return;

} else if (bpParentEntity.getUsersWhoHaveAccessToThisEntity().contains(new ChineseWallUser(bpParentEntity, getUser()))) {

if (bpParentEntity.getChineseWallAccessRights().getCorrespondence()) {

isTabCorrespondenceAccessableToUserAsPerChineseWall = true;

}

if (bpParentEntity.getChineseWallAccessRights().getExecutedDocumentaion()) {

isTabExecutedDocumentationAccessableToUserAsPerChineseWall = true;

}

chineseWallRestrictionMessage = MessageFormat.format(chineseWallRestrictionMessage, entityName);

return;

}

}

}

isTabExecutedDocumentationAccessableToUserAsPerChineseWall = true;

isTabCorrespondenceAccessableToUserAsPerChineseWall = true;

}

protected void loadAvailableOperations() {

List operations = service.operationsForAgreement(getAmendment());

availableActions.clear();

for (Iterator iterator = operations.iterator(); iterator.hasNext(); ) {

ToolbarItem toolbarItem = ToolbarItem.create((Operation) iterator.next(), getAmendment());

if (!veto(toolbarItem)) {

add(toolbarItem);

}

}

}

protected List<MasterAgreement> createAmendmentStatus(List<Long> maList) {

List<MasterAgreement> masterAgreementList = new ArrayList<>();

for (Long ma : maList) {

MasterAgreement masterAgreement = MasterAgreement.get(repository, ma);

if (masterAgreement.getLastAmendment().getSignedDate() != null) {

masterAgreementList.add(masterAgreement);

} else {

masterAgreementList.clear();

break;

}

}

return masterAgreementList;

}

protected void loadEditMAAvailableOperations(List<Long> maList) {

List operations = service.operationsForMasterAgreement(maList);

availableActions.clear();

for (Iterator iterator = operations.iterator(); iterator.hasNext(); ) {

ToolbarItem toolbarItem = ToolbarItem.createToolbar((Operation) iterator.next());

add(toolbarItem);

}

if (availableActions != null && availableActions.containsKey("header.createAmd")) {

List<MasterAgreement> maLastAmemndmentExecutedList = createAmendmentStatus(maList);

if (maLastAmemndmentExecutedList.size() != 0) {

String CollateralOnly = "Collateral Only";

Set<String> updatedAmendmentSubgroup = new HashSet<>();

Set<String> creatAmendmentSubgroup = new HashSet<String>(Arrays.asList("MA Only", "Collateral Only", "MA and Collateral"));

updatedAmendmentSubgroup = creatAmendmentSubgroup;

for (MasterAgreement ma : maLastAmemndmentExecutedList) {

if (creatAmendmentSubgroup.isEmpty()) break;

for (String options : creatAmendmentSubgroup) {

String CSA = ma.hasCSA() ? "Collateral" : "Add Collateral";

if (options.equalsIgnoreCase("MA Only") && !validateMAOnlyOption(ma)) {

updatedAmendmentSubgroup.add(options);

}

if ((options.equalsIgnoreCase(CSA+" Only") || options.equalsIgnoreCase("MA and "+CSA)) && !validateCollateralOptions(ma)) {

updatedAmendmentSubgroup.add(CSA);

updatedAmendmentSubgroup.add("MA and "+CSA);

}

creatAmendmentSubgroup.retainAll(updatedAmendmentSubgroup);

}

}

}

}

}

protected boolean validateMAOnlyOption(MasterAgreement ma) {if ((ma.getAgreementType().isDerivatives() || ma.getAgreementType().isESA() || ma.getAgreementType().isPrimeBrokerage())

&& !(ma.getGroup() != null)) {

return true;

}

return false;

}

protected boolean validateCollateralOptions(MasterAgreement ma) {

String agreementType = ma.getAgreementType().getId().getId();

if (AgreementType.FX_PB_AGREEMENT.equals(agreementType) && AgreementType.getClearingTypes().contains(agreementType)

&& !ma.getAgreementType().isESA()) {

return true;

}

return false;

}

private void add(ToolbarItem toolbarItem) {

List list = (List) availableActions.get(toolbarItem.getTab());

if (list == null) {

list = new ArrayList();

availableActions.put(toolbarItem.getTab(), list);

}

list.add(toolbarItem);

}

protected boolean veto(ToolbarItem toolbarItem) {

if (toolbarItem.getTab().equals(Tabs.CORRESPONDENCE.getCode())) {

if (!isTabCorrespondenceAccessableToUserAsPerChineseWall) {

return true;

}

}if (toolbarItem.getTab().equals(Tabs.EXECUTED_DOCUMENTATION.getCode())) {

if (!isTabExecutedDocumentationAccessableToUserAsPerChineseWall) {

return true;

}

}

return false;

}

public List getAvailableTabs() {

List result = new LinkedList();

Tabs.Tab[] tabs = Tabs.getTabs();

for (int i = 0; i < tabs.length; i++) {

Tabs.Tab tab = tabs[i];

if (getPolicy().canView(masterAgreement, getAmendment(), tab)) {

result.add(tab);

}

if(tab.getName().equalsIgnoreCase(Tabs.PBA_KEY_TERMS.getName())){

if(!repository.isRestrictedEntityPresentInMA(masterAgreement.getId()))

result.remove(tab);

}

if(tab.getName().equalsIgnoreCase(Tabs.LUA_KEY_TERMS.getName())){

if(!repository.isRestrictedEntityPresentInMA(masterAgreement.getId()))

result.remove(tab);

}

String maType = masterAgreement.getAgreementType().getId().getId() + " " + "(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")";

if(repository.isCcpMnaConfigurationMapping(maType)){

if(tab.getName().equalsIgnoreCase("Collateral") || tab.getName().equalsIgnoreCase("Products Covered")){

result.remove(tab);

}

}

if(masterAgreement.isSyntheticRepo()){

if(tab.getName().equalsIgnoreCase(Tabs.CROSS.getName()) || tab.getName().equalsIgnoreCase(Tabs.REPOS.getName())

||tab.getName().equalsIgnoreCase(Tabs.COLLATERAL.getName()) || tab.getName().equalsIgnoreCase(Tabs.CONTACT.getName())

|| tab.getName().equalsIgnoreCase(Tabs.EXECUTED_DOCUMENTATION.getName()) || tab.getName().equalsIgnoreCase(Tabs.CORRESPONDENCE.getName())

|| tab.getName().equalsIgnoreCase(Tabs.REGULATORY.getName())){

result.remove(tab);

}

}

}

return result;

}

public boolean isSyntheticRepo(){

return getAgreementType().toString().equalsIgnoreCase("Synthetic Repo Master Agreement (2024)");

}

public MasterAgreementHeaderDisplay getHeaderDisplay() {

if (masterAgreementHeaderDisplay == null) {masterAgreementHeaderDisplay = new MasterAgreementHeaderDisplay(masterAgreement, getAmendment(), repository,

fileSystemAccess, service, factory, getUser());

masterAgreementHeaderDisplay.setMaStatusFilter((String) this.session.get(MA_STATUS_FILTER));

if(masterAgreement.getGroup()!=null && masterAgreement.getGroup().getId()!= null ) {

masterAgreementHeaderDisplayForLONENTITY();

}

}

return masterAgreementHeaderDisplay;

}

public void masterAgreementHeaderDisplayForLONENTITY() {

List<MasterAgreementLite> selectableNonLondonMAs = masterAgreementHeaderDisplay.getExecuteGroupDisplay().getSelectableNonLondonMAs();

masterAgreementHeaderDisplay.getExecuteGroupDisplay().getSelectableMAs().retainAll(selectableNonLondonMAs);

}

@Override

public void setCookies(Map cookies) {

recentAgreements.loadFromCookieString((String) cookies.get(ShowMasterAgreement.BEAGLE_RECENT_SEARCHES_COOKIE_KEY));

}

public boolean getNegotiationExists() {

return masterAgreement.getNegotiation() != null;

}public List getAvailableActions(String forWhichTab) {

List list = (List) availableActions.get(forWhichTab);

if ("header.status".equals(forWhichTab))

{

if(isLondonEntity() && list!=null)

{

Iterator itr = list.iterator();

while(itr.hasNext()){

Object headerOptionLON = itr.next().toString();

if((("- Execute".equals(headerOptionLON.toString())) || ("- PreExecute".equals(headerOptionLON.toString())) || ("- Ready To Sign".equals(headerOptionLON.toString()))  )){

if(masterAgreement.isMAInNego() || masterAgreement.getAmendment(amendmentId).isInNegotiation()){

itr.remove();

}

}

}

}

}

availableActionForCcpMnaConfig(forWhichTab, list);

return list == null ? Collections.emptyList() : list;

}

public boolean isIsdaLinkedAgreement() {

return masterAgreement.isIsdaLinkedAgreement() || masterAgreement.isGroupDrivenIsdaLinkedAgreement();

}

public List getDraftingActions(List<String> forWhichTab) {

List list = new LinkedList();if (masterAgreement.getLastAmendment().isInNegotiation() /*&& repository.getGroupNumber(masterAgreementId) == null masterAgreement.getGroup() == null*/) {

//Only for ISDA (2002) Group drafting is enabled

if ("ISDA (2002)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())

&& masterAgreement.isStandard() && masterAgreement.getGoverningLaw().getText().equalsIgnoreCase("French")) {

list = getAvailableActions("header.documentAssemblyISDA2002FrenchLaw");

}else if ("ISDA (2002)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())) {

list = getAvailableActions("header.documentAssemblyISDA");

}

if (repository.getGroupNumber(masterAgreementId) == null) {

if ("TBMA ISMA".equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId())) {

list = getAvailableActions("header.documentAssemblyGMRA");} else if ("FBFD (2007)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())

&& (masterAgreement.getActingEntity(Party.COUNTERPARTY) == null)

&& (masterAgreement.getActingEntity(Party.BNP_PARIBAS) == null)) {

list = getAvailableActions("header.documentAssemblyFBF");

return list;

} else if ("FBFD (2013)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())

&& (masterAgreement.getActingEntity(Party.COUNTERPARTY) == null)

&& (masterAgreement.getActingEntity(Party.BNP_PARIBAS) == null)) {

list = getAvailableActions("header.documentAssemblyFBF13");

return list;

} else if ("EMAD (2004)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())

&& (masterAgreement.getActingEntity(Party.COUNTERPARTY) == null)

&& (masterAgreement.getActingEntity(Party.BNP_PARIBAS) == null)

&& (masterAgreement.getBnpLei().equals("KGCEPHLVVKVRZYO1T647"))

&& (masterAgreement.getLastAmendment().getStandardisedContract()!=null&& masterAgreement.getLastAmendment().getStandardisedContract())) {

list = getAvailableActions("header.documentAssemblyEMAD");

return list;

}

}

}

List list1 = new ArrayList();

if (forWhichTab != null && !forWhichTab.isEmpty()) {

for (String whichTab : forWhichTab) {

if (!getAvailableActions(whichTab).isEmpty()) {

list1.add(getAvailableActions(whichTab).get(0));

}

}

}

if (!list1.isEmpty()) {

list.addAll(list1);

}

return list == null ? Collections.emptyList() : list;

}

public void setFileSystemAccess(FileSystemAccess fileSystemAccess) {

this.fileSystemAccess = fileSystemAccess;

}

@Override

public Map getCookiesToWrite() {

HashMap cookies = new HashMap();

cookies.put(BEAGLE_RECENT_SEARCHES_COOKIE_KEY, recentAgreements.toCookieString());

return cookies;

}

public MasterAgreementForm getForm() {

if (form == null) {

form = new MasterAgreementForm(factory);form.setParameters(getParameters());

form.setBeagleRepository(repository);

}

return form;

}

public StringBuilder getAdditonalTermQueryForCog(StringBuilder buffer){

if((StringUtils.isNotEmpty(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId()) ||

StringUtils.isNotBlank(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId()))){

buffer.append(MasterAgreementFields.CP_LEI)

.append(":").append("\"")

.append(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId())

.append("\"").append(" ")

.append(",")

.append(MasterAgreementFields.IS_COGAGREEMENT_TYPE).append(":").append("N").append(" ");

}else{

buffer.append(MasterAgreementFields.CP_ENTITY)

.append(":").append("\"")

.append(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getId())

.append("\"").append(" ")

.append(",")

.append(MasterAgreementFields.IS_COGAGREEMENT_TYPE).append(":").append("N").append(" ");

}

return buffer;

}

public boolean isCogAgreement() {

return getMasterAgreement().getAgreementType().isCogAgreement();}public boolean isCcpClearingChildAgreement() {

String agreementType = masterAgreement.getAgreementType().getId().getId() + " " +

"(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")";

return true;

}

public Map getAdditionalQueryTermForUmbrellaAgreement() {

Entity bnpParibasSigningEntity = getMasterAgreement().getSigningEntity(Party.BNP_PARIBAS);

StringBuilder buffer = new StringBuilder();

if(!getMasterAgreement().getAgreementType().isCogAgreement()){

if (repository.isAgreementInCcpMnaConfScope(masterAgreement.getAgreementType().getId().getId() + " "

+ "(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")",MasterAgreementFields.PARENT_CCP_MNA_CONFIG)) {

buffer.append(Constants.DATA_FIELD_NAME).append(":").append("ccpConfScopeForParent,");

buffer.append(MasterAgreementFields.CCP_AGREEMENT_ID).append(":").append(masterAgreementId+",");

}

buffer.append(MasterAgreementFields.IS_UMBRELLA_AGREEMENT).append(":").append("N ").append("AND ");

buffer.append(MasterAgreementFields.IS_COGAGREEMENT_TYPE).append(":").append("N").append(" ");

if(!getMasterAgreement().isPrivate() && getMasterAgreement().getAgreementType().toString().equalsIgnoreCase("Synthetic Repo Master Agreement (2024)"))

buffer.append(MasterAgreementFields.IS_SYNTHETIC_REPO_ALLOWED_FOR_LINK).append(":").append("Y").append(" ");

if (getMasterAgreement().getAgreementType().isCoDebtor()) {

addConditionForCoDebtor(getMasterAgreement(), buffer);

} else {

addConditionForUmbrellaAgreements(bnpParibasSigningEntity, buffer);

}

}else {

buffer = getAdditonalTermQueryForCog(buffer);

}

return MapBuilder.make("additionalQueryTerm", buffer.toString().trim());

}

private StringBuilder addConditionForUmbrellaAgreements(

Entity bnpParibasSigningEntity, StringBuilder buffer) {

if(StringUtils.isNotEmpty(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId()) ||StringUtils.isNotBlank(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId())) {

if(StringUtils.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId(),"PB-MNA")) {

buffer.append("AND (").append(MasterAgreementFields.CP_LEI)

.append(":").append("\"")

.append(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId())

.append("\"");

coveredOfficesData(buffer);

}

else {

buffer.append("AND ").append(MasterAgreementFields.CP_LEI)

.append(":").append("\"")

.append(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId())

.append("\"").append(" ")

.append(",");

}

}

else {

if(StringUtils.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId(),"PB-MNA")) {

buffer.append("AND (");

coveredOfficesData(buffer);

}

else {

buffer.append("AND ").append(MasterAgreementFields.CP_ENTITY)

.append(":").append("\"")

.append(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getId())

.append("\"").append(" ")

.append("OR ");

}

}for (Iterator<Entity> entityIterator = bnpParibasSigningEntity.getAvailableOffices().iterator(); entityIterator.hasNext(); ) {

Entity entity = entityIterator.next();

buffer.append(MasterAgreementFields.BNP_ENTITY).append(":").append("\"").append(entity.getId()).append("\"");

if (entityIterator.hasNext()) {

buffer.append(" OR ");

}

}

return buffer;

}

public void coveredOfficesData(StringBuilder buffer)

{

List <String> entityIdList = new ArrayList<>();

Set<String> entityIdSet = new HashSet<>();

Set<CoveredOffice> coveredOfficesSet = masterAgreement.getCounterpartyCoveredOffices();

List getOfficesCoveredLEIList =  new ArrayList<>();

List getOfficesCoveredEntityList =  new ArrayList<>();

for (CoveredOffice office : coveredOfficesSet) {

if(StringUtils.isNotEmpty(office.getEntity().getLeiId())){

getOfficesCoveredLEIList.add(office.getEntity().getLeiId());

} else {

getOfficesCoveredEntityList.add(office.getEntity().getId().toString());

}

}

if(getOfficesCoveredLEIList.size()>0){

List<String> getEntityIdList = repository.getEntityIdList(getOfficesCoveredLEIList);

for(String entityId:getEntityIdList) {

if(!getOfficesCoveredEntityList.contains(entityId)){

getOfficesCoveredEntityList.add(entityId);

}

}

}

List<String> getOfficesCoveredMaList = repository.getOfficeCoveredBasedOnEntity(getOfficesCoveredEntityList);

for (String getOfficesCoveredMaId : getOfficesCoveredMaList) {

entityIdList.add(getOfficesCoveredMaId);

}

entityIdSet.addAll(entityIdList);

if(StringUtils.isNotEmpty(masterAgreement.getSigningEntity(Party.COUNTERPARTY).getLeiId())){

buffer.append(" OR ");

}

for (Iterator<String> cpEntityIterator = entityIdSet.iterator(); cpEntityIterator.hasNext(); ) {

String cpEntity = cpEntityIterator.next();

buffer.append("key").append(":").append("\"").append(cpEntity).append("\"");

if (cpEntityIterator.hasNext()) {

buffer.append(" OR ");

}

}

buffer.append("),");

}

public List<String> getDraftingOption() {// CollateralData collateralData = masterAgreement.getCollateralData();

List<String> draftingOptions = new ArrayList<String>();

Set<CollateralData> collateralDataSet = masterAgreement.getAllCollateralData();

for (CollateralData collateralData : collateralDataSet) {

if (isDraftable()) {

if (collateralData != null) {

if ((collateralData.getSignatureDate() == null || collateralData.getAmendmentSignatureDate() == null) && collateralData.getTerminationDate() == null) {

if (StringUtils.isNotBlank(collateralData.getCsaLawId())) {

LookupItem csaLaw = repository.findLookupItemById("CsaLawType", Long.valueOf(collateralData.getCsaLawId()));

if ("NY Law".equals(csaLaw.getText())) {

if (!draftingOptions.contains("header.documentAssemblyNY")) {

draftingOptions.add("header.documentAssemblyNY");

}

} else {

GoverningLaw governingLaw = (GoverningLaw) masterAgreement.getGoverningLaw();

if (governingLaw != null && governingLaw.isEnglish()) {

if (!draftingOptions.contains("header.documentAssemblyEN")) {draftingOptions.add("header.documentAssemblyEN");

}

}

}

}

}

}

}

}

if (!draftingOptions.isEmpty()) {

return draftingOptions;

}

return null;

}

public boolean isDraftable() {

return MA_TYPE_DERIVATIVE.contains(masterAgreement.getAgreementType().toString()) && repository.getGroupNumber(masterAgreementId) == null/*masterAgreement.getGroup() == null*/;

}

public boolean isExecutionMode() {

return executionMode;

}

public void setExecutionMode(boolean executionMode) {

this.executionMode = executionMode;

}

public boolean isPreExecutionMode() {

return preExecutionMode;

}

public void setPreExecutionMode(boolean preExecutionMode) {

this.preExecutionMode = preExecutionMode;

storeInSession(PRE_EXECUTION_MODE_KEY, preExecutionMode);

}

public boolean isReadyToSignMode() {

return readyToSignMode;

}

public void setReadyToSignMode(boolean readyToSignMode) {

this.readyToSignMode = readyToSignMode;

}

public StringBuilder addConditionForCoDebtor(MasterAgreement agreement, StringBuilder buffer) {

buffer.append(Constants.DATA_FIELD_NAME).append(":").append("[a TO z]");

/* if(nettedAgreements.size() > 0)

{

MasterAgreementLite nettedMaLite =	(MasterAgreementLite) nettedAgreements.iterator().next();

MasterAgreement nettedMa = MasterAgreement.get(repository, nettedMaLite.getId());

String nettedMaType = nettedMa.getAgreementType().getId().getId();

buffer.append("AND ").append(MasterAgreementFields.AGREEMENT_TYPE)

.append(":").append("\"")

.append(nettedMaType)

.append("\"");

}

else{

buffer.append(Constants.DATA_FIELD_NAME).append(":").append("i* a* e* o* u*");

}*/

return buffer;

}

public boolean isExecutionAgreement() {

return AgreementType.getClearingTypes().contains(masterAgreement.getAgreementType().getId().getId());

}

public boolean isFXPBAgreement() {return AgreementType.FX_PB_AGREEMENT.equals(masterAgreement.getAgreementType().getId().getId());

}

public boolean isCollateralDocApproved() {

CollateralDocStatusDetails collDocStatus = masterAgreement.getCollateralDocDetailsByDocStatusRule();

if (collDocStatus != null && collDocStatus.getStatus().equals(DocumentClass.STS_APPROVED)) {

return true;

}

return false;

}

public boolean isPrintPreview() {

return masterAgreement.isPreview();

}

public boolean isCollateralControlAgreementViewable() {

return AgreementType.ISDA.equals(masterAgreement.getAgreementType().getId().getId()) || masterAgreement.isAgreementTypeMsfta();

}

public boolean isMultiMultiGroup(){

return masterAgreement.getMultiMultiGroup()!=null ? masterAgreement.getMultiMultiGroup() : false;

}

public boolean isCpCollateralControlAgreementPresent() {

return !masterAgreement.getCounterpartyCollateralControlAgreement().isEmpty();

}

public boolean isBnpCollateralControlAgreementPresent() {return !masterAgreement.getBnpCollateralControlAgreement().isEmpty();

}

public boolean isBnpCcaActive() {

return isCcaActive(masterAgreement.getBnpCollateralControlAgreement());

}

public boolean isCpCcaActive() {

return isCcaActive(masterAgreement.getCounterpartyCollateralControlAgreement());

}

public boolean isCcaActive(Set<CollateralControlAgreement> ccas) {

for (CollateralControlAgreement cca : ccas) {

if (!cca.isDeleted() && cca.getTerminationDate() == null) {

return true;

}

}

return false;

}

public boolean isAdminLocationNyk() {

return Location.LOCATION_CODE_NEW_YORK.equals(masterAgreement.getLastAmendment().getAdminLocation().getCode());

}

public boolean isBnpEntitySecurityCorp() {

return BNP_PARIBAS_SECURITIES_CORP_ENTITY.equals(masterAgreement.getBnpParibasEntity().getId());

}

public boolean isInNegoOrExecutedAgreement() {

return !masterAgreement.isDormant() && !masterAgreement.isTerminated();

}public boolean belongsToAGroup() {

return masterAgreement.belongsToGroup();

}

public Long getGroup() {

return masterAgreement.getGroup().getId();

}

public Integer getGroupSize() {

return belongsToAGroup() ? masterAgreement.getGroup().getAgreements().size() : null;

}

private void addMessages() {

List<String> messages = getHeaderDisplay().getMessages();

for (String message : messages) {

addActionMessage(message);

}

}

public String getEntityName(Long id) {

if (id != null) {

Entity entity = repository.findObject(Entity.class, id);

return entity != null ? entity.toString() : null;

} else

return null;

}

public boolean getIsTabCorrespondenceAccessableToUserAsPerChineseWall() {

return isTabCorrespondenceAccessableToUserAsPerChineseWall;

}

public boolean getIsTabExecutedDocumentationAccessableToUserAsPerChineseWall() {

return isTabExecutedDocumentationAccessableToUserAsPerChineseWall;

}
public String getChineseWallRestrictionMessage() {

return chineseWallRestrictionMessage;

}

public boolean shouldRegisterWithNSD() {

boolean returnwhat = Boolean.FALSE;

String nsdRepoCode = repository.getNSDRepositoryCode(masterAgreement.getMaster().getMasterAgreementId());

if (masterAgreement.getLastLiveSignedAmendment() != null && masterAgreement.getLastLiveSignedAmendment().getAmendmentId().equals(amendmentId) /*&& getUser().isLegalUser()*/) {

if (masterAgreement.getAmendment(amendmentId).getSignedDate() != null && masterAgreement.getAmendment(amendmentId).getTerminatedDate() == null) {

if (!masterAgreement.getRegulators().isEmpty()) {

for (Regulator regulator : masterAgreement.getRegulators()) {

if (regulator.getRegulator() != null && "FSFM".equalsIgnoreCase(regulator.getRegulator()) && regulator.getTradeRepository() != null && "NSD".equalsIgnoreCase(regulator.getTradeRepository())) {

returnwhat = Boolean.TRUE;

}

}

}

}

} else if (masterAgreement.getAmendment(amendmentId).getSignedDate() != null && masterAgreement.getAmendment(amendmentId).getTerminatedDate() != null && !BeagleStringUtils.isEmpty(nsdRepoCode) /*&& getUser().isLegalUser()*/) {

returnwhat = Boolean.TRUE;

}

return returnwhat;

}

@Override

public String getNSDRepositoryCode() {

if (getMasterAgreementId() == null) {

return "";

}

return repository.getNSDRepositoryCode(getMasterAgreementId());

}

public boolean shouldNSDAlert() {

return masterAgreement.getAmendment(amendmentId).getStatus().equals(AmendmentStatus.EXECUTED)

&& repository.getNSDRepositoryCode(masterAgreement.getMaster().getMasterAgreementId()) != null;

}

public boolean isRussianMailSent() {

return russianMailSent;

}

public void setRussianMailSent(boolean russianMailSent) {

this.russianMailSent = russianMailSent;

}

public String getAgreementDateForMA(String maId) {return BeagleDateFormat.format(MasterAgreement.get(repository, Long.parseLong(maId)).getAgreementDate());

}

public Map<String, String> getTabAuditPermissions() {

return service.getTabAuditPermissions();

}

public boolean isGroupReportToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 3L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isConsolidatedAuditHistoryToBeShown(MasterAgreement ma) {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(),

73L,getDocCategory());

isConsolidatedAuditToBeShown = userRolePermissions.canAccessGroupPermission();

return isConsolidatedAuditToBeShown;

}

public boolean isAddCollateralToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 15L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}public boolean isModifyCollateralToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 16L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isDeleteCollateralToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 17L,getDocCategory());

return "Read/Write".equals(userRolePermissions.getAccessRights()) ? true : false;

}

public boolean isCSAModifyToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 19L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCSAAuditToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 20L,getDocCategory());

return userRolePermissions.isPermissionReadOnly();

}

public boolean isCCAAddToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 21L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCCAModifyToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 22L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCCADeleteToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 23L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isDisplayAllCorrespondenceToBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 54L,getDocCategory());

return userRolePermissions.isPermissionReadOnly();

}public boolean isCanActionProtocolTracking() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 36L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanViewNegotiationDetails() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 50L,getDocCategory());

return userRolePermissions.isPermissionReadOnly();

}

public boolean isCanActionCorrespondence() {

List<UserRolePermissions> userRolePermissions = repository.fetchUserRoleTabPermission(getUser().getRole().getId(), Arrays.asList(52L, 53L, 51L),getDocCategory());

for (UserRolePermissions userRolePermission : userRolePermissions) {

if (userRolePermission.isPermissionReadWrite())

return true;

}

return false;

}public boolean isCanAddNettingAgreementCovered() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 6L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanAddNettingAgreementLinked() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 7L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanDoddFrankBeShown() {

List<UserRolePermissions> userRolePermissions = repository.fetchUserRoleTabPermission(getUser().getRole().getId(), Arrays.asList(33L, 34L, 35L, 36L),getDocCategory());

for (UserRolePermissions permission : userRolePermissions) {

if (permission.canAccessGroupPermission())

return true;

}

return false;

}public boolean isCanDFProtocol1BeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 33L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanDFProtocol2BeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 34L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanSegIABeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 35L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanProtocolTrackingBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 36L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}public boolean isCanEMIRBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 37L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanFATCABeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 38L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanRRBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 39L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanBRRDBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 40L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}//TODO check access

public boolean isCanBMRBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 112L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanSFTRRegBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 113L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanHIREACTBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 41L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isCanAddRussianRegulator() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 39L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}public boolean isCanModifyAmendmentDetail() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 30L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanTransferDocuments() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 46L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanEmailDocuments() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 47L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}

public boolean isCanSubGroupBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 5L,getDocCategory());

return userRolePermissions.isPermissionReadWrite();

}public boolean isCanFINRAACTBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 96L,getDocCategory());

return !userRolePermissions.isPermissionDenied();

}

public boolean isPrintReviewToBeShown() {

List actions = getAvailableActions("header.print");

return CollectionUtils.isNotEmpty(actions);

}

public boolean isCanViewExecuteDocument(MasterAgreement ma) {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(),

43L,getDocCategory());

return userRolePermissions.canAccessGroupPermission();

}

public String getInitialTab() {

List<UserRolePermissions> userRolePermissions = repository.fetchUserRoleTabPermission(getUser().getRole().getId(), TAB_ID_LIST,getDocCategory());

for (UserRolePermissions permission : userRolePermissions) {

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.OFFICE.getName())

&& permission.canAccessGroupPermission())

initialTab = "OFFICE";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.PRODUCT.getName())

&& permission.canAccessGroupPermission())

initialTab = "PRODUCT";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.LEGAL.getName())

&& permission.canAccessGroupPermission())

initialTab = "LEGAL";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.CROSS.getName())

&& permission.canAccessGroupPermission())

initialTab = "CROSS";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.REGULATORY.getName())

&& permission.canAccessGroupPermission())

initialTab = "REGULATORY";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.EXECUTED_DOCUMENTATION.getName())

&& permission.canAccessGroupPermission())

initialTab = "EXECUTED_DOCUMENTATION";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.CORRESPONDENCE.getName())

&& permission.canAccessGroupPermission())

initialTab = "CORRESPONDENCE";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.CONTACT.getName())

&& permission.canAccessGroupPermission())

initialTab = "CONTACT";

if (permission.getPermissionCategorization().getGroupPermission().equalsIgnoreCase(Tabs.CFL.getName())

&& permission.canAccessGroupPermission())

initialTab = "CFL";

}

return initialTab;

}

public String getView() {

return view;

}

public void setView(String view) {

this.view = view;

}

public Set getFields() {

return fields;

}

public void setFields(String[] fields) {

this.fields = new HashSet(Arrays.asList(fields));

}

public boolean isOutputField(String fieldId) {

return null == (fields) ? false : fields.contains(fieldId);

}

public String dateFormatter(Date date) {

String formattedDate = null;

if (date != null) {

formattedDate = BeagleDateFormat.format(date);

}

return formattedDate;

}

public String getCounterpartyEntityActing() {

if (masterAgreement.getActingEntity(Party.COUNTERPARTY) != null) {

return masterAgreement.getActingEntity(Party.COUNTERPARTY).getName() + "-" + masterAgreement.getActingEntity(Party.COUNTERPARTY).getLocation().getCode();

} else {

return "";

}

}

public String getCounterpartyFundManagingCompany() {

if (masterAgreement.getFundManagingCompany(Party.COUNTERPARTY) != null) {

return masterAgreement.getFundManagingCompany(Party.COUNTERPARTY).getName() + "-" + masterAgreement.getFundManagingCompany(Party.COUNTERPARTY).getLocation().getCode();

} else {

return "";

}

}public String getCounterpartyFundWithCompartments() {

if (masterAgreement.getFundWithCompartments(Party.COUNTERPARTY) != null) {

return masterAgreement.getFundWithCompartments(Party.COUNTERPARTY).getName() + "-" + masterAgreement.getFundWithCompartments(Party.COUNTERPARTY).getLocation().getCode();

} else {

return "";

}

}

public String getBNPParibasEntityActing() {

if (masterAgreement.getActingEntity(Party.BNP_PARIBAS) != null) {

return masterAgreement.getActingEntity(Party.BNP_PARIBAS).getName() + "-" + masterAgreement.getActingEntity(Party.BNP_PARIBAS).getLocation().getCode();

} else {

return "";

}

}

public String getBNPParibasFundManagingCompany() {

if (masterAgreement.getFundManagingCompany(Party.BNP_PARIBAS) != null) {

return masterAgreement.getFundManagingCompany(Party.BNP_PARIBAS).getName() + "-" + masterAgreement.getFundManagingCompany(Party.BNP_PARIBAS).getLocation().getCode();

} else {

return "";

}

}

public String getBNPParibasFundWithCompartments() {if (masterAgreement.getFundWithCompartments(Party.BNP_PARIBAS) != null) {

return masterAgreement.getFundWithCompartments(Party.BNP_PARIBAS).getName() + "-" + masterAgreement.getFundWithCompartments(Party.BNP_PARIBAS).getLocation().getCode();

} else {

return "";

}

}

public String getIncorporatedIn() {

Entity cpLegalEntity = masterAgreement.getSigningEntity(Party.COUNTERPARTY);

Country incorporated = cpLegalEntity.getIncorporated();

return incorporated != null ? "[" + incorporated.getCode() + "]" + " " + incorporated.getName() : "";

}

public String getCpStateName() {

Entity cpLegalEntity = masterAgreement.getSigningEntity(Party.COUNTERPARTY);

return cpLegalEntity.getStateName() != null ? cpLegalEntity.getStateName() : "";

}

public String getCounterpartyActingClassificationText() {

if (masterAgreement.getActingEntity(Party.COUNTERPARTY) != null) {

return masterAgreement.getActingEntity(Party.COUNTERPARTY).getClassification();

} else {

return "";

}

}public String getLastCorrespondenceComment(User thisUser) {

determineIfChineseWallAllowsToAccessCorrespondence(masterAgreement);

String comment = "";

if (ChineseWallEntitiesAccessRights.RESTRICTED.equals(isCorrespondenceAccessible)) {

if (usersWhoHaveAccessToThisMA != null) {

List<String> userIdList = Arrays.asList(usersWhoHaveAccessToThisMA.split("\\s*,\\s*"));

if (userIdList.contains(thisUser.getId())) {

DocumentTypeFilter filter = filter("CORRESPONDENCE", null);

List docGroups = NegotiationService.getDocumentGroups(masterAgreement, filter, repository);

if (!docGroups.isEmpty()) {

comment = ((NegotiationDocument) ((LinkedList) docGroups.get(0)).get(0)).getDetail();

}

}

}

} else if (ChineseWallEntitiesAccessRights.YES.equals(isCorrespondenceAccessible)) {

DocumentTypeFilter filter = filter("CORRESPONDENCE", null);

List docGroups = NegotiationService.getDocumentGroups(masterAgreement, filter, repository);

if (!docGroups.isEmpty()) {comment = ((NegotiationDocument) ((LinkedList) docGroups.get(0)).get(0)).getDetail();

}

}

return comment != null ? comment : "";

}

private void determineIfChineseWallAllowsToAccessCorrespondence(MasterAgreement masterAgreement) {

List<Entity> chineseWallEntities = new ArrayList<Entity>();

chineseWallEntities.addAll(repository.getBPChineseWallEntities());

isCorrespondenceAccessible = ChineseWallEntitiesAccessRights.YES; //String constants give better performance than enum

usersWhoHaveAccessToThisMA = "";

Entity bpEntity = masterAgreement.getBnpParibasEntity();

Entity cpEntity = masterAgreement.getCounterpartyEntity();

Entity bpParentEntity = null;

if (!bpEntity.isParent()) {

bpParentEntity = bpEntity.getParent();

}

if (bpEntity.isParent()) {

bpParentEntity = bpEntity;

}

if (cpEntity.isPrivate()) {

if (bpParentEntity != null && chineseWallEntities.contains(bpParentEntity)) {

if (bpParentEntity.getChineseWallAccessRights().getCorrespondence()) {

isCorrespondenceAccessible = ChineseWallEntitiesAccessRights.RESTRICTED; //Restritcted access to selected users

ArrayList list = new ArrayList();

for (ChineseWallUser chineseWallUser : bpParentEntity.getUsersWhoHaveAccessToThisEntity()) {

if (chineseWallUser != null) {

list.add(chineseWallUser.getKey().getUser().getId());

}

}

usersWhoHaveAccessToThisMA = BeagleStringUtils.convertCollectionToCommaSeparatedString(list);

} else {

isCorrespondenceAccessible = ChineseWallEntitiesAccessRights.NO;

}

}

}

}

protected static DocumentTypeFilter filter(final String category, final String filter) {

return new DocumentTypeFilter() {

@Override

public boolean accept(NegotiationDocumentType docType) {

return docType != null && category.equals(docType.getCategory()) && matches(filter, docType);

}

};

}

private static boolean matches(String documentTypeFilter, NegotiationDocumentType docType) {

return StringUtils.isBlank(documentTypeFilter) ||(documentTypeFilter.equalsIgnoreCase(docType.getText()) || (StringUtils.isBlank(docType.getText()) && documentTypeFilter.toUpperCase().startsWith("OTHER")));

}

public String getSubGroupDescription() {

Iterator<SubGroup> subGroups = masterAgreement.getGroup().getSubGroups().iterator();

while (subGroups.hasNext()) {

SubGroup subGroup = subGroups.next();

if (subGroup.getMasterAgreementLite().getId().equals(masterAgreementId)) {

return subGroup.getSubGroupName();

}

}

return "";

}

public boolean isCorrespondenceDocumentAvailable() {

Set correspondencDocuments = masterAgreement.getAllNegotiationDocuments().stream()

.filter(negotiationDocument -> negotiationDocument.getType().getCategory().equals(NegotiationDocumentType.CORRESPONDENCE_TYPE))

.collect(Collectors.toSet());

return CollectionUtils.isNotEmpty(correspondencDocuments) ? true : CollectionUtils.isNotEmpty(masterAgreement.getAllCorrespondenceSharedDocuments());

}/* public boolean isEmailDraftEMADToBeShown(){

if("EMAD (2004)".equalsIgnoreCase(masterAgreement.getAgreementType().toString())){

return true;

}

return false;

}*/

public long getDelay() {

return delay;

}

public void setDelay(long delay) {

this.delay = delay;

}

protected void clearSession() {

try {

Thread.sleep(TimeUnit.SECONDS.toMillis(delay));

StrutsUtils.clearSession();

} catch (InterruptedException e) {

BeagleRuntimeException.handle(e);

}

}

public boolean isCdeaWithoutIsdaAgreement() {

return isdaCdeaLinkageControlService.isCdeaPermittedWithoutIsda(

masterAgreement.getAgreementType(),

masterAgreement.getActingEntity(Party.COUNTERPARTY),

masterAgreement.getBnpParibasEntity());

}

public static String getGroupStatusChangeBlockMessage() {

return IsdaCdeaLinkageControlConstants.CDEA_IS_CREATED_WITHOUT_LINKAGE;

}

public String getModule() {

return module;

}

public void setModule(String module) {this.module = module;

bannerModule = BannerModule.getModule(module);

}

public String getBannerMessage() {

return bannerModule.getMessage();

}

public String getBannerTitle() {

return bannerModule.getTitle();

}

public boolean getCdeaAgreement() {

bannerModule = BannerModule.getModule(BannerModule.UNLINKED_CDEA.getId());

return  masterAgreement.getAgreementType().isLinkedCdeaAgreementType() ;

}

public BannerModule getBannerModule() {

return bannerModule;

}

public void setBannerModule(BannerModule bannerModule) {

this.bannerModule = bannerModule;

}

public boolean isTabEditable() {

return !this.toString().startsWith(ShowMasterAgreement.class.getName());

}

public boolean isGroupTabOlderView() {

return groupTabOlderView;

}

public void setGroupTabOlderView(boolean groupTabOlderView) {

this.groupTabOlderView = groupTabOlderView;

}

public MaEditOptions getMaEditOptions() {MaEditOptions maEditOptions = new MaEditOptions();

addOptions(maEditOptions, "header.status");

addOptions(maEditOptions, "header.modify");

if(!isCogAgreement())

addOptions(maEditOptions, "header.createAmd");

addOptions(maEditOptions, "header.modifyAmd");

addOptions(maEditOptions, "header.copyMa");

List options = new ArrayList();

if(isDraftable()) {

options = getDraftingOption();

}

List draftOptions = getDraftingActions(options);

if( CollectionUtils.isNotEmpty(draftOptions) ) {

maEditOptions.setOptions(draftOptions);

}

addOptions(maEditOptions, "header");

if(!isCanEditCogMa())

maEditOptions=null;

return maEditOptions;

}

public MaEditOptions getMaEditOptionsForMultipleMAs() {

MaEditOptions maEditOptions = new MaEditOptions();

List listOfStatus = (List) availableActions.get("header.status");

if(listOfStatus != null){

maEditOptions.setOptions(listOfStatus);}

return maEditOptions;

}private void addOptions(MaEditOptions maEditOptions, String header) {

List headerOptions = getAvailableActions(header);

if (CollectionUtils.isNotEmpty(headerOptions)) {

maEditOptions.setOptions(headerOptions);

}

}

public boolean isLondonEntity (){

boolean flag = false;

if(masterAgreement.isMAInNego() || masterAgreement.getAmendment(amendmentId).isInNegotiation()) {

if(null!=masterAgreementHeaderDisplay) {

List<CoveredOfficeDisplay> coveredOfficeDisplayList = masterAgreementHeaderDisplay.getBnpParibasCoveredOfficesDisplay();

if (CollectionUtils.isNotEmpty(coveredOfficeDisplayList)) {

if (coveredOfficeDisplayList.size() == 1) {

Iterator iterator = coveredOfficeDisplayList.iterator();

while (iterator.hasNext()) {

CoveredOfficeDisplay coveredOfficeDisplay = (CoveredOfficeDisplay) iterator.next();

if ("[BNPL-LON][PARB-LON]".contains(coveredOfficeDisplay.getEntity())) {

flag = true;setMessage("Agreements with BNP PARIBAS London branch only are not permitted.Please review the branch coverage for BNP Paribas.");

}

}

}

}

}

}

return flag;

}

public class MaEditOptions {

public List<List> leftColumnOptions = new ArrayList<>();

public List<List> rightColumnOptions = new ArrayList<>();

public List getLeftColumnOptions() {

return leftColumnOptions;

}

public List getRightColumnOptions() {

return rightColumnOptions;

}

public void setOptions(List options) {

if(leftColumnOptions.size() == rightColumnOptions.size()) {

leftColumnOptions.add(options);

} else {

rightColumnOptions.add(options);

}

}

}

public String getXpaWarning(String id){

return XPAAgreementConstants.getXPAMessage(id);

}

@Override

public AuditStatus getGroupEditJobAuditStatus() {

if( masterAgreement.belongsToGroup() ) {

return repository.getGroupEditJobAuditCount(masterAgreement.getGroup().getId(), getUser());} else {

return new AuditStatus();

}

}

public boolean isGroupEditJobAlreadySubmitted() {

return groupEditJobAlreadySubmitted;

}

public void setGroupEditJobAlreadySubmitted(boolean groupEditJobAlreadySubmitted) {

this.groupEditJobAlreadySubmitted = groupEditJobAlreadySubmitted;

}

public boolean isBilateralAgreement(){

return masterAgreement.getAgreementType().isBilateral();

}

public boolean isMaInArt71BrrdScope(){

return regulationsAndProtocolsService.isMaInArt68To71Scope(masterAgreement);

}

public boolean isMaInArt55BrrdScope(){

return regulationsAndProtocolsService.isMaInBrrdScope(masterAgreement);

}

public boolean isMaInIsdaBMRScope(){

return regulationsAndProtocolsService.isMaInIsdaBMRScope(masterAgreement);

}

public boolean isMaInSFTRRegScope(){

return regulationsAndProtocolsService.isMaInSftrScope(masterAgreement);

}public boolean isCanArt68To71BRRDBeShown() {

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 110L);

return !userRolePermissions.isPermissionDenied();

}

public String isArt68To71Compliant(){

return masterAgreement.isGoverningLawEuropean() ? "Y" : regulationsAndProtocolsService.isArt68To71Compliant(masterAgreement);

}

public String isArt55BrrdCompliant(){

return masterAgreement.isGoverningLawEuropean()? "Y" : regulationsAndProtocolsService.isArt55BrrdCompliant(masterAgreement);

}

public String isDoddFrankCompliant(){

return regulationsAndProtocolsService.isDoddFrankCompliant(masterAgreement);

}

public String isBmrCompliant(){

return regulationsAndProtocolsService.isBmrCompliant(masterAgreement);

}

public String isEmirCompliant(){

//return regulationsAndProtocolsService.isEmirCompliant(masterAgreement);

return masterAgreement.getEmirSpecialClauseJoins().stream().anyMatch(a->a.getSpecialClauseValue() == 1L) ? "Y" : "N";

}public String isFatcaCompliant(){

return regulationsAndProtocolsService.isFatcaCompliant(masterAgreement);

}

public String isHireActCompliant(){

return regulationsAndProtocolsService.isHireActCompliant(masterAgreement);

}

public boolean isConfiguationMissingInManageParameter(List list){

return list.size()==0;

}

public boolean isAgreementTypePledgeGMSLA(){

return AgreementType.GMSLA_PLEDGE.equalsIgnoreCase(masterAgreement.getAgreementType().getId().getId());

}

public boolean isCanBP2SReverseCRDSBeShown() {

return masterAgreement.isDerivativesAgreement() && masterAgreement.getAgreementType().isBilateral() && repository.canBP2SReverseCRDSBeShown(masterAgreementId, getUser().getId());

}

public List<Long> getGroupIdForLinkedCCA(Long cpCCAGrpId, Long bnpCCAGrpId) {

List<Long> ids = new ArrayList<>();

if( BeagleNumberUtils.isNonZero(cpCCAGrpId) ) {

ids.add(cpCCAGrpId);

}

if( BeagleNumberUtils.isNonZero(bnpCCAGrpId) ) {

ids.add(bnpCCAGrpId);

}if( !ids.isEmpty() ) {

return repository.getGroupIdForLinkedCCA(ids);

}

return new ArrayList();

}

public void setPeCrdsConfigHelper(PeCrdsConfigHelper peCrdsConfigHelper) {

this.peCrdsConfigHelper = peCrdsConfigHelper;

}

public List<Long> getMexicoEntity(){

return peCrdsConfigHelper.getDistinctEntities();

}

public boolean shouldHideLei(String classificationCode){

return (getUser().isAgreementViewer() && classificationCode !=null

&& classificationCode.equalsIgnoreCase("UND"));

}

public boolean isCategoryRestricted() {

return AgreementCategoryHelper.isCategoryRestricted(masterAgreement.getAgreementType().getCategory());

}

//Jira 12099

public boolean isMaISDA_1992_2002(){

return (masterAgreement.getAgreementType().toString().equals("ISDA (2002)")||masterAgreement.getAgreementType().toString().equals("ISDA (1992)"));

}
public boolean isMaFBFD(){

return (masterAgreement.getAgreementType().toString().equals("FBFD (2013)")||masterAgreement.getAgreementType().toString().equals("FBFD (2007)") || masterAgreement.getAgreementType().toString().equals("AFB (1994)"));

}

public boolean isMaFBF(){

return (masterAgreement.getAgreementType().toString().equals("FBF (2001)"));

}

public boolean isEsaCategory(){

return AgreementCategoryHelper.isESACategory(masterAgreement.getAgreementType().getCategory());

}

public void setAmdExeCancelled(boolean amdExeCancelled) {

this.amdExeCancelled = amdExeCancelled;

}

public boolean getAmdExeCancelled() {

return this.amdExeCancelled;

}

private void deleteLatestAmd(AmendmentType amendmentType, Amendment amendment) {

Long lastAmendmentType = null;

if ( amendmentType!= null) {

lastAmendmentType = amendmentType.getId();

}

if (amendment != null) {

collateralOpsService.updateAmendmentForCollateralDocStatus(masterAgreement, amendment.getAmendmentId(), lastAmendmentType);for (Iterator<NegotiationDocumentForExecDoc> iterator = masterAgreement.getAllExecutedDocuments().iterator(); iterator.hasNext(); ) {

NegotiationDocumentForExecDoc doc = iterator.next();

if (doc.getAmendmentId() != null && doc.getAmendmentId().equals(amendment.getAmendmentId())) {

doc.setAmendmentId(null);

}

}

repository.delete("Amendment", amendment);

amendmentId=masterAgreement.getLastAmendment().getAmendmentId();

}

}

public void setCollateralOpsService(CollateralOpsService collateralOpsService){

this.collateralOpsService = collateralOpsService;

}

public List getSelectedMAPb() {

return selectedMAPb;

}

public void setSelectedMAPb(String selectedMAPb) {

this.selectedMAPb = selectedMAPb != null ? Arrays.asList(selectedMAPb.split(",")).stream().map(ma -> Long.parseLong(ma) ).collect(Collectors.toList()) : Collections.emptyList() ;

}

public boolean isCounterpartyGuaranteeToBeShown(){

if( masterAgreement.isGuaranteeCpFlagNull())

return true;

return masterAgreement.getGuaranteeFlagCp() ;

}

public boolean isBnpGuaranteeToBeShown(){

if( masterAgreement.isGuaranteeBnpFlagNull())

return true;

return masterAgreement.getGuaranteeFlagBnp() ;

}

public boolean isGuaranteeDetailMissing(){

return (((masterAgreement.getCounterpartyGuarantees().isEmpty() && masterAgreement.getCounterpartyGuaranteesMissingEntity().isEmpty()) && masterAgreement.getGuaranteeFlagCp()) ||

((masterAgreement.getBnpParibasGuarantees().isEmpty() && masterAgreement.getBnpParibasGuaranteesMissingEntity().isEmpty()) && masterAgreement.getGuaranteeFlagBnp()))

&& (masterAgreement.isExecuted() ||  masterAgreement.isPreExecuted() || masterAgreement.isReadyToSign());

}public boolean isChildHavingMoreThanOneBNP(){

return masterAgreement.getAgreementType().isCogAgreement() && masterAgreement.getCloseOutNettingGroupDetails()!=null

&& (!masterAgreement.getCloseOutNettingGroupDetails().isCrossBNPPEntities()) && repository.checkIfLinkedAgreementsHaveMoreThanOneBnpEntity(masterAgreement.getId());

}

public boolean isChildHavingMoreThanOneRmpm(){

return masterAgreement.getAgreementType().isCogAgreement() && masterAgreement.getCloseOutNettingGroupDetails()!=null

&& (!masterAgreement.getCloseOutNettingGroupDetails().isCrossClientLegalEntity()) && repository.checkIfLinkedAgreementsHaveMoreThanOneRmpm(masterAgreement.getId());

}

public Boolean getPermissionToDisplayRmpmCode(){

Entity entity = masterAgreement.getCounterpartyEntity();

if((entity.isUndisclosedEntity() && getUser().isLegalUserAndAdmin()) || !entity.isUndisclosedEntity()){

return true;

}

return false;

}

public String getRmpmCodeToDisplay(){

return masterAgreement.getCounterpartyEntity().getRmpmIinCode();}public Boolean isRmpmCodeNull(){

return StringUtils.isEmpty(masterAgreement.getCounterpartyEntity().getRmpmIinCode());

}

public String getNameForCoveredAgreementTab(){

if( masterAgreement.getMasterNettingIdForCog()!=null && !masterAgreement.getAgreementType().isUmbrellaAgreement()){

return CLOSE_OUT_GROUP;

}else if(masterAgreement.getAgreementType().isCogAgreement() || masterAgreement.getAgreementType().isUmbrellaAgreement()){

return COVERED_AGREEMENTS;

} else if (masterAgreement.getMasterNettingId() != null) {

if (getCcpMnaConfiguration()) {

return CLEARING_PARENT_AGREEMENT;

}

return MASTER_NETTING_AGREEMENT;

}else{

return MASTER_NETTING_AGREEMENT;

}

}

public Boolean isFamilyExecutionMaster(){

return masterAgreement.getAgreementType().isExecutionMaster();

}

public boolean getCcpMnaConfiguration() {

if (repository.isAgreementInCcpMnaConfScope(repository.getMaTypeUsingMaId(masterAgreement.getMasterNettingId()),MasterAgreementFields.PARENT_CCP_MNA_CONFIG)) {

return true;

}

return false;

}public void removeHeaderForCcpMnaConfig(List list) {

Iterator itr = list.iterator();

while (itr.hasNext()) {

Object headerOption = itr.next().toString();

if (headerOption != null) {

itr.remove();

}

}

}

public void removeHeaderForCcpMnaConfigForGroup(List list, boolean manageGrpFlag) {

Iterator itr = list.iterator();

while (itr.hasNext()) {

Object headerOption = itr.next().toString();

if (headerOption != null) {

if (manageGrpFlag) {

if (!("Execute Group / Single MA".equals(headerOption.toString()))) {

if (MasterAgreementFields.HEADER_OPTION_REMOVED_FOR_CCP.contains(headerOption.toString())) {

itr.remove();

}

}

} else {

if (MasterAgreementFields.HEADER_OPTION_REMOVED_FOR_CCP.contains(headerOption.toString()) || ("Execute Group / Single MA".equals(headerOption.toString()))) {

itr.remove();

}

}

}

}

}

public void availableActionForCcpMnaConfig(String forWhichTab, List list) {

if ("header.status".equals(forWhichTab)) {if ((masterAgreement.getMasterNettingId() != null && repository.isAgreementInCcpMnaConfScope(masterAgreement.getAgreementType().getId().getId() + " "

+ "(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")",MasterAgreementFields.CHILD_CCP_MNA_CONFIG))) {

if (masterAgreement.getLastAmendment().getAmendmentId() != 0) {

if (masterAgreement.getLastAmendment().isInNegotiation()) {

Iterator itr = list.iterator();

while (itr.hasNext()) {

Object headerOption = itr.next().toString();

if (!("- Execute".equals(headerOption.toString()))) {

itr.remove();

}

}

} else {

removeHeaderForCcpMnaConfig(list);

}

} else {

removeHeaderForCcpMnaConfig(list);

}

}

}

if ("header.modify".equals(forWhichTab)) {

if ((masterAgreement.getMasterNettingId() != null && repository.isAgreementInCcpMnaConfScope(masterAgreement.getAgreementType().getId().getId() + " "repository.isAgreementInCcpMnaConfScope(masterAgreement.getAgreementType().getId().getId() + " "

+ "(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")",MasterAgreementFields.CHILD_CCP_MNA_CONFIG))) {

Iterator itr = list.iterator();

while (itr.hasNext()) {

Object headerOption = itr.next().toString();

if ((("- Agreement Type".equals(headerOption.toString())) || (("- Entities".equals(headerOption.toString()))))) {

itr.remove();

}

}

}

}

if ("Manage_Group".equals(forWhichTab)) {

boolean manageGrpFlag = false;

if ((masterAgreement.getMasterNettingId() != null && repository.isAgreementInCcpMnaConfScope(masterAgreement.getAgreementType().getId().getId() + " "

+ "(" + masterAgreement.getAgreementType().getYear() + "-" + masterAgreement.getAgreementType().getMonth() + ")",MasterAgreementFields.CHILD_CCP_MNA_CONFIG)) && permissionForManageGroup().equalsIgnoreCase("true")) {

List<MasterAgreementLite> agreementLites = masterAgreement.getGroup().getAgreements();

for(MasterAgreementLite ma : agreementLites){

MasterAgreement ma1= MasterAgreement.get(repository, ma.getId());

if (ma1.getLastAmendment().getAmendmentId() != 0) {

if (ma1.getLastAmendment().isInNegotiation()) {

manageGrpFlag=true;

removeHeaderForCcpMnaConfigForGroup(list,manageGrpFlag);

break;

}

}

}if(!manageGrpFlag){

removeHeaderForCcpMnaConfigForGroup(list,manageGrpFlag);

}

}

}

}

public String permissionForManageGroup(){

UserRolePermissions userRolePermissions = repository.fetchUserRolePermission(getUser().getRole().getId(), 1L,masterAgreement.getAgreementType().getDocCategory().getId());

return userRolePermissions.isPermissionReadWrite()? "true":"false";

}

public boolean isCanEditCogMa() {

if (!getUser().isPbUser())

return true;

boolean isDerivCog = AgreementCategoryHelper.isDERIVCategory(masterAgreement.getAgreementType().getCategory()) && masterAgreement.getAgreementType().isCogAgreement();

boolean isPbCategory = AgreementCategoryHelper.isPBCategory(masterAgreement.getAgreementType().getCategory());

return isDerivCog || isPbCategory;

}

public String getCloseOutNettingControl() {

return closeOutNettingControl;

}

public void setCloseOutNettingControl(String closeOutNettingControl) {

this.closeOutNettingControl = closeOutNettingControl;

}

public List<Long> getCloseNettingControlList(){

List<Long> result = new ArrayList<>();

if(!AgreementCategoryHelper.isCategoryRestricted(getMasterAgreement().getAgreementType().getCategory())){

result= repository.getScopeForCloseOutNettingControl(getMasterAgreement().getCounterpartyEntity().getId(), getMasterAgreement().getCloseOutNetting(), getMasterAgreement().getAgreementType().getCategory(), null)

.stream().mapToLong(s->s.getId()).boxed().collect(Collectors.toList());

}

return result;

}

public boolean getPbEsaCategory() {

return AgreementCategoryHelper.isCategoryRestricted(masterAgreement.getAgreementType().getCategory());

}

}
