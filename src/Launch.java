import com.sss.git.*;
public class Launch {
	public static void main(String args[]){
		int nParams = args.length;
		String usage = "usage: java -classpath . -jar gitrobot.jar <apiUrl> <github id> <github password> <command> [<args>]\n\n";
		usage += "The most commonly used commands are:\n";
		usage += "\tcreate_repo <repo name> [<repo description> <homepage url>]		����github��\n";
		usage += "\tget <repo name> <local path>		��github��������Դ��ָ���ı���·��\n";
		usage += "\tget_dir <repo name> <remote path> <local path>		��github���ָ���ļ������ص�����·��\n";
		usage += "\tget_file <repo name> <remote file path> <local directory path>		��github������ָ�����ļ���ָ���ı���·��\n";
		usage += "\tput <repo name> <local path>		�ѱ�����Դ�ύ��ָ����github����\n";
		usage += "\tput_dir <repo name> <local path> <remote path>		�ѱ����ļ����ύ��github���ָ��·��\n";
		usage += "\tput_file <repo name> <local file path>	<remote directory path> 	�ύ�����ļ���Զ�̿��ָ��·��\n";
		usage += "\tdelete <repo name> <remote path>	��github����ɾ��ָ�����ļ���\n";
		usage += "\tdelete_repo <repo name>			ɾ��github��\n";

		if(nParams < 4){
			System.out.print(usage);
			return;
		}
		String apiUrl = args[0];
		String strId = args[1];
		String strPw = args[2];
		String strCommand = args[3];
		if(strCommand.equalsIgnoreCase("create_repo")){
			if(nParams < 5){
				System.out.print(usage);
				return;
			}
			String strRepoName = args[4];
			String strDescription = nParams >= 6 ? args[5] : "";
			String strHomepageUrl = nParams >= 7 ? args[6] : "";
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.createRepository(strRepoName, strDescription, strHomepageUrl);
			return;
		}
		else if(strCommand.equalsIgnoreCase("delete_repo")){
			if(nParams < 5){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.deleteRepository(strRepoName);
			return;
		}
		else if(strCommand.equalsIgnoreCase("get")){
			if(nParams < 6){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strLocalPath = args[5];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.getContents(strRepoName, "", strLocalPath);
			return;
		}
		else if(strCommand.equalsIgnoreCase("get_dir")){
			if(nParams < 7){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strRemotePath = args[5].equals("/") ? "" : args[5];
			String strLocalPath = args[6];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.getContents(strRepoName, strRemotePath, strLocalPath);
			return;
		}
		else if(strCommand.equalsIgnoreCase("get_file")){
			if(nParams < 7){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strRemotePath = args[5].equals("/") ? "" : args[5];
			String strLocalPath = args[6];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.getSingleContent(strRepoName, strRemotePath, strLocalPath);
			return;
		}
		else if(strCommand.equalsIgnoreCase("put")){
			if(nParams < 6){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strLocalPath = args[5];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.updateContents(strRepoName, strLocalPath, "");
			return;
		}
		else if(strCommand.equalsIgnoreCase("put_dir")){
			if(nParams < 7){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strLocalPath = args[5];
			String strRemotePath = args[6].equals("/") ? "" : args[6];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.updateContents(strRepoName, strLocalPath, strRemotePath);
			return;
		}
		else if(strCommand.equalsIgnoreCase("put_file")){
			if(nParams < 7){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strLocalPath = args[5];
			String strRemotePath = args[6].equals("/") ? "" : args[6];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.updateSingleContent(strRepoName, strLocalPath, strRemotePath);
			return;
		}
		else if(strCommand.equalsIgnoreCase("delete")){
			if(nParams < 6){
				System.out.print(usage);
				return;
			}
			String strRepoName = strId + "/" + args[4];
			String strRemotePath = args[5];
			
			GitRobot robot = new GitRobot(apiUrl, strId, strPw);
			robot.deleteContents(strRepoName, strRemotePath);
			return;
		}
	}
}
