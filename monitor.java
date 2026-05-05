This is FileSystemAccess.java

package com.bnpparibas.beagle.kernel.services;

import java.io.*;

import java.nio.file.Path;

import java.util.Arrays;

import java.util.List;

import java.util.Optional;

import com.bnpparibas.beagle.documents.actions.DocumentsAction;

import com.bnpparibas.beagle.kernel.logging.BeagleLogger;

import com.bnpparibas.beagle.kernel.util.ZLibUtils;

public class FileSystemAccess {

private static final BeagleLogger LOGGER = BeagleLogger.getLogger(FileSystemAccess.class);

private ZLibUtils zLibUtils = new ZLibUtils();

private DataDirectoryProvider directoryProvider;

public void setDataDirectoryProvider(DataDirectoryProvider directoryProvider) {

this.directoryProvider = directoryProvider;

}

public void load(String filename, OutputStream stream) throws IOException {

zLibUtils.decompressAndDecrypt(getFile(filename), stream);

}

public File getFile(String filename) {

return new File(directoryProvider.getRootDirectoryForAttachments(), filename);

}

public boolean exists(String filename) throws IOException {

return getFile(filename).exists();

}

public boolean checkFullAccess() throws IOException {

File directory = directoryProvider.getRootDirectoryForAttachments();

return directory.canWrite() && directory.canRead();

}

public void save(String filename, File content) throws IOException {

File targetFile = getFile(filename);

zLibUtils.encryptAndCompress(content, targetFile);

}

public void saveImportLog(String filename, File content) throws IOException {

File targetFile = getImportLogFile(filename);

zLibUtils.compress(content, new FileOutputStream(targetFile));// Need to Check

}

public File getFileForExecutedDocument(String filename){

return new File(directoryProvider.getExecutedDocuments(), separatorsToSystem(filename));

}

public File getFileForExecutedDocumentForESA(String filename){

return new File(directoryProvider.getExecutedDocumentsForESA(), separatorsToSystem(filename));

}

public File getImportLogFile(String filename) {if (filename.contains("icei")) {

return new File(directoryProvider.getRootDirectoryForIsdaCdeaImport(), filename);

} else if (filename.contains("aagei")) {

return new File(directoryProvider.getRootDirectoryForDataImport(), filename);

}  else if(filename.contains("bp2sei"))  {

return new File(directoryProvider.getRootDirectoryForBp2sImport(), filename);

}else if(filename.contains("isdaRspei")){

return new File(directoryProvider.getRootDirectoryForBp2sImport(), filename);

}else if(filename.contains("isdaJmpei")){

return new File(directoryProvider.getRootDirectoryForBp2sImport(), filename);

}else if(filename.contains("isdaBmrei")){

return new File(directoryProvider.getRootDirectoryForIsdaBmrImport(), filename);

}else if(filename.contains("isdaBailInArticle")) {

return new File(directoryProvider.getRootDirectoryForIsdaBailInArticleImport(), filename);

}else if(filename.contains("AldopGroups")){return new File(directoryProvider.getRootDirectoryForAldopGroupsImport(), filename);

}else if(filename.contains("Generic_Upload")){

return new File(directoryProvider.getRootDirectoryForGenericUpload(), filename);

}else if(filename.contains("ExecutedDocumentUpload")){

return new File(directoryProvider.getRootDirectoryForExecutedDocUpload(), filename);

}else if(filename.contains("IsdaRspBrrd2")) {

return new File(directoryProvider.getRootDirectoryForIsdaRspBrrd2Import(),filename);

}else if(filename.contains("isdaPRDRei")){

return new File(directoryProvider.getRootDirectoryForBp2sImport(), filename);

}else if(filename.contains("Ccp")){

return new File(directoryProvider.getRootDirectoryForCcpExcel(),filename);

}

return null;

}

public void delete(String filename) {

boolean isDeleted = getFile(filename).delete();

if( !isDeleted ) {

LOGGER.error("Unable to delete file "+filename);

}

}

public BeagleFile getFile(String fileNameOnDisk, String realFileName) {

BeagleFile file = new BeagleFile(directoryProvider.getRootDirectoryForAttachments(), fileNameOnDisk);

file.setFileNameAsUploaded(realFileName);

return file;

}

public DataDirectoryProvider getDirectoryProvider() {

return directoryProvider;

}

public void loadImportLogs(String filename, OutputStream outputStream) throws IOException {

zLibUtils.decompress(getImportLogFile(filename), outputStream);

}

public boolean copyFile(String srcFileName, String newFileName) throws IOException {

File srcFile = getFile(srcFileName);

if( srcFile.exists() ) {

try(FileInputStream fis  = new FileInputStream(srcFile);

FileOutputStream fos = new FileOutputStream(getFile(newFileName))) {

byte[] buf = new byte[1024];

int i = 0;

while ((i = fis.read(buf)) != -1) {

fos.write(buf, 0, i);

}

} catch (Exception e) {

LOGGER.error("Unable to create copy of file "+srcFileName);

throw e;

}

return true;

}

return false;

}

private String separatorsToSystem(String res) {

if (res==null) return null;

if (File.separatorChar=='\\') {

// From Windows to Linux/Mac

return res.replace('/', File.separatorChar);

} else {

// From Linux/Mac to Windows

return res.replace('\\', File.separatorChar);}}}