package uk.co.harcourtprogramming.docitten;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import uk.co.harcourtprogramming.internetrelaycats.ExternalService;
import uk.co.harcourtprogramming.internetrelaycats.InternetRelayCat;
import uk.co.harcourtprogramming.internetrelaycats.RelayCat;

public class DistroService extends ExternalService {

	public DistroService(InternetRelayCat inst) {
		super(inst);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startup(RelayCat r) {
		List<HelpService> helpServices = r.getServicesByClass(HelpService.class);

		if (!helpServices.isEmpty())
		{
			HelpService.HelpInfo help = new HelpService.HelpInfo("Distro Service",
				"The distro service broadcasts configuration updates to the "
				+ "standard DoC Ubuntu distribution. The distro config repo is "
				+ "checked for changes every 5 minutes.");
			helpServices.get(0).addHelp("distro", help);
		}
	}
	
	private class Distro {
		private Git git;
		private ObjectId head;
		private String name;
		
		/**
		 * 
		 * @param f The git repository of the distro to represent
		 * @throws IOException 
		 */
		public Distro(File f, String name) throws IOException {
			this.git = Git.open(f);
			this.name = name;
			this.updateHead();
		}
		
		/**
		 * Updates the distro to point to the newest HEAD, and returns the old HEAD Object
		 * @return
		 * @throws IOException 
		 * @throws IncorrectObjectTypeException 
		 * @throws AmbiguousObjectException 
		 * @throws RevisionSyntaxException 
		 */
		private ObjectId updateHead() throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
			ObjectId oldHead = this.head;
			try {
				this.head = this.git.getRepository().resolve("HEAD");
			} catch (Exception e) {
				// FIXME: I don't know what to do here.
			}
			return oldHead;
		}
		
		public Iterable<RevCommit> fetchUpdates() {
			Iterable<RevCommit> ret;
			try {
				ObjectId oldHead = this.updateHead();
				ret = this.git.log().addRange(oldHead, this.head).call();
			} catch (Exception e) {
				ret = new ArrayList<RevCommit>();
			}
			return ret;
		}
		
		public String commitToString(RevCommit c) {
			StringBuilder sb = new StringBuilder();
			sb.append("CSG updated ").append(this.name).append(" configuration: ");
			sb.append(c.getShortMessage());
			return sb.toString();
		}
		
		public Iterable<String> fetchStringUpdates() {
			Iterable<RevCommit> upds = this.fetchUpdates();
			List<String> strs = new LinkedList<String>();
			for (RevCommit c : upds) {
				strs.add(this.commitToString(c));
			}
			return strs;
		}
	}
}
