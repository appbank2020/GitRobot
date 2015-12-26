package com.sss.git;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import Decoder.BASE64Decoder;


/**
 * ��Github API����������⣬��Դ���ύ�����أ�ɾ���ȹ���
 * @author Appbank
 * @version 1.2.1
 * @since 2015-07-07
 */
public class GitRobot {
	public static void main(String args[]){
	}
	private String apiUrl = "";
	private String userId = "";
	private String password = "";
	boolean bFinished = false;
	private Thread mThread;
	Scanner scan;

	public GitRobot(){
	}
	/**
	 * ���캯��
	 * @param userId git�û���
	 * @param password git����
	 */
	public GitRobot(String apiUrl, String userId, String password){
		this.apiUrl = apiUrl;
		this.userId = userId;
		this.password = password;
		checkProcessState();
	}
	public void checkProcessState(){
		scan = new Scanner( System.in );
		mThread = new Thread(
		new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
		        while ( !bFinished ) {
		        	if(scan.hasNextLine()){
			            String message = scan.nextLine();
			            if ( message.equals( "stop" ) ) {
			            	scan.close();
			            	System.exit(0);
			                break;
			            }
		        	}   	
		        }
		        scan.close();	
			}
		});
		mThread.start();
		
		new Thread(
			new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
			        while ( !bFinished ) {
			        	try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			        System.exit(0);
				}
			}).start();
	}
	/**
	 * ����github��
	 * @param strRepoName github����
	 * @param strDescription ���� 
	 * @param strUrl ��ҳ
	 * @return void
	 */
	public void createRepository(String strRepoName, String strDescription, String strUrl){
		GitHub github;
		try {
			System.out.println("Create repo started!\t"+strRepoName);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			github.createRepository(strRepoName, strDescription, strUrl, true);
			System.out.println("Create repo finished!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * ɾ��ָ����repository
	 * @param strRepoName Ҫɾ����github����
	 * @return void
	 */
	public void deleteRepository(String strRepoName){
		GitHub github;
		GHRepository repo;
		try {
			System.out.println("Delete repo started!\t"+strRepoName);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			
			repo = github.getRepository(strRepoName);
			repo.delete();

			System.out.println("Delete repo finished!");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * �Ӹ�repository����������Դ
	 * @param strRepoName github����
	 * @param strLocalPath �����ļ���·��
	 * @return void
	 */
	public void getContents(String strRepoName, String strRemotePath, String strLocalPath){
		GitHub github;
		GHRepository repo;
		try {
			if(strLocalPath.charAt(strLocalPath.length()-1) != '\\')
				strLocalPath += "\\";
			System.out.println("Download started!\t"+strRepoName + "/" + strRemotePath+" => " + strLocalPath);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			
			repo = github.getRepository( strRepoName);
			List<GHContent> contents = repo.getDirectoryContent(strRemotePath);
			Iterator it = contents.iterator();
			while(it.hasNext()){
				GHContent content = (GHContent)it.next();
				if(content.isDirectory()){
					getRepoDirectory(content, strRemotePath, strLocalPath);
				}
				else {
					String strFileName = strLocalPath + content.getName();
				
					if(content.getDownloadUrl() != null && content.getSize() < 1024 * 1024){
						writeFile(content.read(), strFileName);
					}
					else if(content.getGitUrl() != null){
						writeFileFromGitUrl(content.getGitUrl(), strFileName);
					}
				}
			}
			System.out.println("Download finished!");

		} catch(FileNotFoundException e1){
			System.out.println("Error:"+e1.getCause().getMessage()+"��Դ������");
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * ���ص����ļ�
	 * @param strRepoName �ֿ���
	 * @param strRemoteFilePathWithName Զ�̿������Դ·��������src/test.txt)
	 * @param strLocalFilePath Ҫ���ص��ı���·������Ҫ�����ļ���
	 * @return void
	 */
	public void getSingleContent(String strRepoName, String strRemoteFilePathWithName, String strLocalFilePath){
		GitHub github;
		GHRepository repo;
		try {
			if(strLocalFilePath.charAt(strLocalFilePath.length()-1) != '\\')
				strLocalFilePath += "\\";
			System.out.println("Download started!\t"+strRepoName+"/"+strRemoteFilePathWithName+" => " + strLocalFilePath);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			
			int nLastSlashPos = strRemoteFilePathWithName.lastIndexOf('/');
			String strRemotePath = nLastSlashPos <= 0 ? "" : strRemoteFilePathWithName.substring(0, nLastSlashPos);
			String strRemoteFileName = nLastSlashPos == -1 ? strRemoteFilePathWithName : strRemoteFilePathWithName.substring(nLastSlashPos+1);
			repo = github.getRepository(strRepoName);
			List<GHContent> contents = repo.getDirectoryContent(strRemotePath);
			Iterator it = contents.iterator();
			boolean bDownloaded = false;
			while(it.hasNext()){
				GHContent content = (GHContent)it.next();
				if(content.getName().equalsIgnoreCase(strRemoteFileName)){
					if(content.isDirectory()){
						errHandle("This is a directory.");
						return;
					}
					else{
						String strFileName = strLocalFilePath + content.getName();
						if(content.getDownloadUrl() != null && content.getSize() < 1024 * 1024){
							writeFile(content.read(), strFileName);
							bDownloaded = true;
						}
						else if(content.getGitUrl() != null){
							writeFileFromGitUrl(content.getGitUrl(), strFileName);
							bDownloaded = true;
						}
					}
				}
			}
			if(bDownloaded)
				System.out.println("Download finished!");
			else
				errHandle("��Ŀ¼");
		} catch(FileNotFoundException e1){
			System.out.println("Error:"+e1.getCause().getMessage()+"��Դ������");
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
		bFinished = true;
	}

	/**
	 * ��ָ����repo����ɾ��ָ�����ļ���
	 * @param strRepoName github����
	 * @param strDirectory Ҫɾ�����ļ���
	 * @return void
	 */
	public void deleteContents(String strRepoName, String strDirectory){
		GitHub github;
		GHRepository repo;
		try {
			System.out.println("Delete started!\t"+strRepoName+"/" + strDirectory);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			
			repo = github.getRepository(strRepoName);
			List<GHContent> lContents = null;
			try{
				lContents = repo.getDirectoryContent(strDirectory);
				Iterator<GHContent> it = lContents.iterator();
				while(it.hasNext()){
					GHContent content = it.next();
					if(content.isDirectory()){
						deleteContentsDir(repo, content.getPath());
						//content.delete("delete dir");
					}
					else{
						System.out.println("Deleted!\t" + content.getPath());
						content.delete("delete dir");
					}
				}
			}
			catch(FileNotFoundException ex){
				System.out.println(strDirectory + " does not exists.");
			}
			System.out.println("Delete finished!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * �ύ��Դ��ָ����repo��
	 * @param strRepoName github����
	 * @param strPath ����·��
	 * @return void
	 */
	public void updateContents(String strRepoName, String strLocalPath, String strRemotePath){
		GitHub github;
		GHRepository repo;
		try {
			if(strLocalPath.charAt(strLocalPath.length()-1) != '\\')
				strLocalPath += "\\";
			if(strRemotePath.length() > 0 && strRemotePath.charAt(strRemotePath.length()-1) == '/')
				strRemotePath = strRemotePath.substring(0, strRemotePath.length()-1);
			System.out.println("Upload started!\t"+strLocalPath + " => " + strRepoName+"/"+strRemotePath);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			repo = github.getRepository( strRepoName);

			Path startingDir = Paths.get(strLocalPath);
			UpdateFileFinder finder = new UpdateFileFinder(repo, strRemotePath, strLocalPath, "*.*");
			Files.walkFileTree(startingDir, finder);
	        finder.done();
			System.out.println("Upload finished!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(e.getClass().equals(FileNotFoundException.class)){
				System.out.println("Error:"+e.getCause().getMessage()+"�ֿⲻ����");
				System.exit(0);
			}
			else
				errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * �ύ�����ļ���Զ�̿��ָ��·��
	 * @param strRepoName �ֿ���
	 * @param strLocalFilePathWithName �����ļ�·���������ļ�����
	 * @param strRemotePath Զ��·�����������ļ�����
	 * @return void
	 */
	public void updateSingleContent(String strRepoName, String strLocalFilePathWithName, String strRemotePath){
		GitHub github;
		GHRepository repo;
		try {
			if(strRemotePath.length() > 0 && strRemotePath.charAt(strRemotePath.length()-1) == '/')
				strRemotePath = strRemotePath.substring(0, strRemotePath.length()-1);
			System.out.println("Upload started!\t"+strLocalFilePathWithName + " => " + strRepoName + "/" + strRemotePath);
			github = GitHub.connectToEnterprise(getApiUrl(), getUserId(), getPassword());
			repo = github.getRepository( strRepoName);

			byte[] fileContents = {};
        	try {
        		File file = new File(strLocalFilePathWithName);
        		if(!file.isFile() || !file.exists()){
        			errHandle(strLocalFilePathWithName+" �ļ�������");
        		}
        		int nLastBackSlashPos = strLocalFilePathWithName.lastIndexOf('\\');
    			String strLocalFilePath = nLastBackSlashPos == -1 ? "" : strLocalFilePathWithName.substring(0, nLastBackSlashPos+1);
    			String strLocalFileName = nLastBackSlashPos == -1 ? strLocalFilePathWithName : strLocalFilePathWithName.substring(nLastBackSlashPos+1);
    			Path path = Paths.get(strLocalFilePath, strLocalFileName);
				fileContents = Files.readAllBytes(path);
				
				String commitMsg = new Date().toString();
				updateContent(repo, fileContents, strRemotePath, strLocalFileName, commitMsg);
        	} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			System.out.println("Upload finished!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(e.getClass().equals(FileNotFoundException.class)){
				System.out.println("Error:"+e.getCause().getMessage()+"�ֿⲻ����");
				System.exit(0);
			}
			else
				errHandle(e.getMessage());
		}
		bFinished = true;
	}
	/**
	 * �ݹ麯��. ����github����ĳһ��Ŀ¼��������Դ������·��
	 * @param content github����ĳһ��Ŀ¼����
	 * @param strPath ����·��
	 * @return void
	 */
	private void getRepoDirectory(GHContent content, String strRemotePath, String strLocalPath){
		try {
			PagedIterable<GHContent> pi = content.listDirectoryContent();
			Iterator<GHContent> it = pi.iterator();
			while(it.hasNext()){
				GHContent content1 = it.next();
				if(content1.isDirectory())
					getRepoDirectory(content1, strRemotePath, strLocalPath);
				else{
					int nPathStartPos = strRemotePath.length()==0 || strRemotePath.charAt(strRemotePath.length()-1)=='/' ? strRemotePath.length() : strRemotePath.length()+1;
					String strFileName = strLocalPath + content1.getPath().substring(nPathStartPos);
					if(content1.getDownloadUrl() != null && content1.getSize() < 1024 * 1024){
						writeFile(content1.read(), strFileName);
					}
					else if(content1.getGitUrl() != null){
						writeFileFromGitUrl(content1.getGitUrl(), strFileName);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			errHandle(e.getMessage());
		}
	}
	/**
	 * �����ļ�������·��
	 * @param is �ļ�������
	 * @param strFileName �����ļ���
	 * @return void
	 */
	private void writeFile(InputStream is, String strFileName){
		byte[] cb = new byte[1024];
		int nSize = 0;
		try {
			File file = new File(strFileName);
			File dir = file.getParentFile();
			if(!dir.exists())
				dir.mkdirs();
			OutputStream fw = new FileOutputStream(file);
			while((nSize = is.read(cb, 0, 1024)) > 0){
				fw.write(cb, 0, nSize);
			}
			fw.close();
			System.out.println("Downloaded!\t" + strFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Write File Error: " + e.getMessage());
		}
	}
	private void writeFileFromGitUrl(String strGitUrl, String strFileName){
		try {
			File file = new File(strFileName);
			File dir = file.getParentFile();
			if(!dir.exists())
				dir.mkdirs();
			String strJSON = getDataFromUrl(strGitUrl);
			JSONObject jsonObj = (JSONObject)JSONSerializer.toJSON(strJSON);
			if(jsonObj.has("content")){
				BASE64Decoder decoder = new BASE64Decoder();
				byte[] decodeData = decoder.decodeBuffer(jsonObj.getString("content"));
				FileOutputStream os = new FileOutputStream(file);
				os.write(decodeData);
				os.close();
			}
			
			System.out.println("Downloaded!\t" + strFileName);
		}catch(JSONException ex){
			System.out.println("JSON Parse Error: " + ex.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Write File Error: " + e.getMessage());
		}
	}

	/**
	 * �ύ�ļ���github���ָ����λ��
	 * @param repo github�����
	 * @param fileContents �ļ�����
	 * @param strFilePathInServer github����ı���·��
	 * @param strFileName �ļ���
	 * @param commitMsg �ύ��Ϣ
	 * @return void
	 */
	private void updateContent(GHRepository repo, byte[] fileContents, String strFilePathInServer, String strFileName, String commitMsg){
		try{
			
			List<GHContent> contents = repo.getDirectoryContent(strFilePathInServer);//URLEncoder.encode(strFilePathInServer, "UTF-8"));//
			Iterator it = contents.iterator();
			boolean bUpdated = false;
			while(it.hasNext()){
				GHContent content = (GHContent)it.next();
				String strFileNameInRemote = URLEncoder.encode(content.getName(), "UTF-8");
				if(strFileNameInRemote.equalsIgnoreCase(strFileName)){
					if(content.isDirectory()){
						errHandle("This is a directory.");
						return;
					}
					else{
						content.update(fileContents, commitMsg);
						System.out.println("Updated!\t"+strFilePathInServer+"/" + strFileName);
						bUpdated = true;
					}
				}
			}
			if(!bUpdated){
				String strRemotePath = strFilePathInServer.length()== 0 ? strFileName : strFilePathInServer + "/" + strFileName;
				repo.createContent(fileContents, commitMsg, strRemotePath);
				System.out.println("Created!\t"+strRemotePath);
			}
		}catch(FileNotFoundException ex){
			String strRemotePath = strFilePathInServer.length()== 0 ? strFileName : strFilePathInServer + "/" + strFileName;
			try{
				repo.createContent(fileContents, commitMsg, strRemotePath);
			}catch(IOException ex1){
				errHandle(ex1.getMessage());
			}
			System.out.println("Created!\t"+strRemotePath);
		} catch (IOException e) {
			errHandle(e.getMessage());
		}
	}
	/**
	 * �ݹ麯��. ɾ��github����ָ�����ļ���
	 * @param repo github�����
	 * @param strDirectory github������ļ���·��
	 * @return void
	 */
	private void deleteContentsDir(GHRepository repo, String strDirectory){
		try {
			List<GHContent> lContents = repo.getDirectoryContent(strDirectory);
			Iterator<GHContent> it = lContents.iterator();
			while(it.hasNext()){
				GHContent content = it.next();
				if(content.isDirectory())
					deleteContentsDir(repo, content.getPath());
				else{
					content.delete("delete dir");
					System.out.println("Deleted!\t" + content.getPath());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			errHandle(e.getMessage());
		}
	}

	/**
	 * �����ļ��ࡣʹ��SimpleFileVisitor
	 *
	 */
	class UpdateFileFinder extends SimpleFileVisitor<Path> {
		private GHRepository repo = null;
		private String basePath = "";
		private String remotePath = "";
	    private final PathMatcher matcher;
	    private int numMatches = 0;
	
	    UpdateFileFinder(GHRepository repo, String remotePath, String basePath, String pattern) {
	    	this.repo = repo;
	    	this.basePath = basePath;
	    	this.remotePath = remotePath;
	        matcher = FileSystems.getDefault()
	                .getPathMatcher("glob:" + pattern);
	    }
	
	    /**
	     *  ����ģʽƥ������ļ�
	     */
	    // ��glob pattern�����ļ����ļ���
	    void find(Path file) {
	        Path name = file.getFileName();
	        if(name != null && matcher.matches(name)){
		        System.out.println(name);
	        	byte[] fileContents = {};
	        	try {
					fileContents = Files.readAllBytes(file);
					String fileName = file.toString();
					String strFullPathInServer = remotePath + "/" + fileName.substring(basePath.length()).replace("\\", "/");
					if(strFullPathInServer.charAt(0) == '/')
						strFullPathInServer = strFullPathInServer.substring(1);
					String strRemotePath = "";
					String strFileName = strFullPathInServer;
					int nSlashPos = strFullPathInServer.lastIndexOf("/");
					if(nSlashPos != -1){
						strRemotePath = strFullPathInServer.substring(0, nSlashPos);
						strFileName = strFullPathInServer.substring(nSlashPos+1);
					}
					//strFileName  = URLEncoder.encode(strFileName, "UTF-8");
					String commitMsg = new Date().toString();
					updateContent(repo, fileContents, strRemotePath, strFileName, commitMsg);
		            numMatches++;
	        	} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	
	    /**
	     *  ����������ɺ�ִ��
	     */
	    void done() {
	        //System.out.println("Total Updated: " + numMatches);
	    }
	
	    /**
	     *  ��������һ���ļ�֮��ִ��ģʽƥ��
	     */
	    @Override
	    public FileVisitResult visitFile(Path file,
	            BasicFileAttributes attrs) {
	        find(file);
	        return CONTINUE;
	    }
	
	    /**
	     *  ��������һ���ļ���֮��ִ��ģʽƥ��
	     */
	    @Override
	    public FileVisitResult preVisitDirectory(Path dir,
	            BasicFileAttributes attrs) {
	        find(dir);
	        return CONTINUE;
	    }
	
	    @Override
	    public FileVisitResult visitFileFailed(Path file,
	            IOException exc) {
	        System.err.println(exc);
	        return CONTINUE;
	    }
	}
	/**
	 * �Ӹ�URL�������
	 * @param url URL
	 * @return String ��Դ����
	 */
	private String getDataFromUrl(String url){
		String strData = "";
		BufferedReader in = null;
		try{
			URL obj = new URL(url);
			if(url.startsWith("https")){
				String strHost = url.substring(8, url.indexOf("/", 9));
				HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
				con.setConnectTimeout(65000);
				con.setReadTimeout(60000);
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("Host", strHost);
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
				HostnameVerifier hv = new HostnameVerifier() {  
					@Override
			        public boolean verify(String urlHostName, SSLSession session) {  
			            System.out.println("Warning: URL Host: " + urlHostName + " vs. "  
			                               + session.getPeerHost());  
			            return true;  
			        }
			    };  
			    trustAllHttpsCertificates();  
			    con.setDefaultHostnameVerifier(hv);  
				String strEncode = con.getContentEncoding();
				if("gzip".equals(strEncode))
					in = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
				else
					in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
			}
			else{
				String strHost = url.substring(7, url.indexOf("/", 8));
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setConnectTimeout(15000);
				con.setReadTimeout(10000);
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
				con.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				con.setRequestProperty("Accept-Encoding", "gzip, deflate");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				con.setRequestProperty("Cache-Control","max-age=0");
				con.setRequestProperty("Cookie","");
				con.setRequestProperty("Host", strHost);
				Thread.sleep(200);
				String strEncode = con.getContentEncoding();
				if("gzip".equals(strEncode))
					in = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
				else
					in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			strData = response.toString();
			in.close();
		}catch(Exception ex){
			//ex.printStackTrace();
			System.out.println(ex);
		}finally{
			try{
				if(in != null)
					in.close();
			}catch(Exception ex1){}
		}
		return strData.trim();
	}
	private static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
                .getSocketFactory());  
    }    
    static class miTM implements javax.net.ssl.TrustManager,  
            javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
    }

	/**
	 * ������Ϣ����
	 * @param strMsg ������Ϣ
	 * @return void
	 */
	private void errHandle(String strMsg){
		try{
			JSONObject jsonObj = (JSONObject)JSONSerializer.toJSON(strMsg);
			String msg = jsonObj.getString("message");
			String strErrorMsg = "Error: " + msg +". ";
			if(jsonObj.has("errors")){
				JSONArray errArray = jsonObj.getJSONArray("errors");
				Iterator<JSONObject> it = errArray.iterator();
				while(it.hasNext()){
					JSONObject errObj = it.next();
					if(errObj.has("message"))
						strErrorMsg += errObj.getString("message")+". ";
				}
			}
			System.out.println(strErrorMsg);
		}catch(JSONException ex){
			//ex.printStackTrace();
			System.out.println("Error: " + strMsg);
		}
	}
	
	public String getApiUrl() {
		return apiUrl;
	}
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
