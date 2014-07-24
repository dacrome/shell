package org.osiam.shell.command.select;

import java.io.IOException;
import java.util.List;

import org.osiam.client.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Group;
import org.osiam.shell.command.OsiamAccessCommand;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.ShellBuilder;
import de.raysha.lib.jsimpleshell.ShellDependent;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.exception.CLIException;

/**
 * This class contains all commands with that show(select) groups.
 * 
 * @author rainu
 */
public class SelectGroupCommand extends OsiamAccessCommand implements ShellDependent {

	private Shell shell;
	
	public SelectGroupCommand(AccessToken at, OsiamConnector connector) {
		super(at, connector);
	}
	
	@Override
	public void cliSetShell(Shell theShell) {
		this.shell = theShell;
	}

	@Command(description = "Show the given group.")
	public Object showGroup(
			@Param(name = "groupName", description = "The name of the group.")
			String groupName){
		
		Group group = getGroup(groupName);
		
		if(group == null) return "There is no group with the name \"" + groupName + "\"!";
		return group;
	}
	
	@Command(description = "Show all groups.")
	public List<Group> showAllGroups(){
		return connector.getAllGroups(accessToken);
	}
	
	@Command(description = "Search for existing groups by a given filter.")
	public void searchGroups(
			@Param(name = "filter", description = "The filter string.")
			String filter) throws CLIException, IOException{

		searchGroups(filter, 1L, 5, "displayName", true);
	}
	
	@Command(description = "Search for existing groups by a given Query.")
	public void searchGroups(
			@Param(name = "filter", description = "The filter string.")
			String filter,
			@Param(name="index", description = "The index (1-based) of the first resource.")
			Long startIndex,
			@Param(name = "limit", description = "The number of returned groups per page.")
			Integer limit,
			@Param(name = "orderBy", description = "Sort the groups by the given attribute.")
			String orderBy,
			@Param(name = "asc", description = "Should the groups sorted ascending?")
			Boolean ascending) throws CLIException, IOException{
		
		final QueryBuilder builder = new QueryBuilder()
								.filter(filter)
								.startIndex(startIndex)
								.count(limit);
		
		if(orderBy != null){
			if(ascending){
				builder.ascending(orderBy);
			}else{
				builder.descending(orderBy);
			}
		}
		
		StringBuilder prompt = new StringBuilder("searchGroups(\"");
		prompt.append(filter);
		prompt.append("\" ");
		prompt.append(ascending ? "^|" : "v|");
		prompt.append(orderBy);
		prompt.append(ascending ? "|^" : "|v");
		prompt.append(")");
		
		final Shell subShell = ShellBuilder.subshell(prompt.toString(), shell)
									.addHandler(new GroupSearchResultCommand(accessToken, connector, builder.build()))
								.build();
		
		subShell.processLine(GroupSearchResultCommand.COMMAND_NAME_NEXT);
		subShell.commandLoop();
	}
}
