// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package audittrail.actions;

import audittrail.log.CreateLogObject;
import audittrail.proxies.TypeOfLog;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;

/**
 * This java action creates a log object of the input object. The current and old member values of this object are stored in the log.
 * 
 * This action assumes the object is being deleted and does not validate all members for changes.
 * 
 * The Log Object that is being returned can be ignored, the Java committed the entity.
 */
public class CreateLogRecordChangesBeforeDelete extends CustomJavaAction<IMendixObject>
{
	private IMendixObject AuditableObject;

	public CreateLogRecordChangesBeforeDelete(IContext context, IMendixObject AuditableObject)
	{
		super(context);
		this.AuditableObject = AuditableObject;
	}

	@java.lang.Override
	public IMendixObject executeAction() throws Exception
	{
		// BEGIN USER CODE
		IMendixObject logObject = CreateLogObject.createAuditLogItems(this.AuditableObject, this.getContext(), TypeOfLog.Delete);
		//The Java Action create auditLogLines commits the LogObject
		
		return logObject;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "CreateLogRecordChangesBeforeDelete";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
