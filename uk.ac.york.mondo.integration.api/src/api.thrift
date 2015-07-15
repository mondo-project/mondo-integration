namespace java uk.ac.york.mondo.integration.api


enum ModelElementChangeType {
		/* The model element was added to the model. */ ADDED 
		/* The model element was removed from the model. */ REMOVED 
		/* The contents of the model element were changed. */ UPDATED 
}

struct CollaborationGitResourceReference {
	 /* The URI of the repository containing the resource. */ 1: required string repositoryUri,
	 /* The name of the Git branch to which new commits should be pushed. */ 2: required string branch,
	 /* The SHA1 identifier of the commit with the resource. */ 3: required string commit,
}

struct CollaborationLockQuerySpec {
	 /* Fully qualified name of the pre-existing query. */ 1: required string patternFQN,
}

struct CollaborationQueryBinding {
	 /* Name of the query parameter being bound. */ 1: required string name,
	 /* Value to be bound to the query parameter. */ 2: required string value,
}

struct CollaborationResourceReference {
	 /* The URI of the repository containing the resource. */ 1: required string repositoryUri,
}

struct CollaborationSvnResourceReference {
	 /* The URI of the repository containing the resource. */ 1: required string repositoryUri,
	 /* The revision number containing the resource. */ 2: required string revision,
	 /* The path to the resource within the SVN repository. */ 3: required string filePath,
}

struct Credentials {
	 /* Username for logging into the VCS. */ 1: required string username,
	 /* Password for logging into the VCS. */ 2: required string password,
}

struct DerivedAttributeSpec {
	 /* The URI of the metamodel to which the derived attribute belongs. */ 1: required string metamodelUri,
	 /* The name of the type to which the derived attribute belongs. */ 2: required string typeName,
	 /* The name of the derived attribute. */ 3: required string attributeName,
	 /* The (primitive) type of the derived attribute. */ 4: optional string attributeType,
	 /* The multiplicity of the derived attribute. */ 5: optional bool isMany,
	 /* A flag specifying whether the order of the values of the derived attribute is significant (only makes sense when isMany=true). */ 6: optional bool isOrdered,
	 /* A flag specifying whether the the values of the derived attribute are unique (only makes sense when isMany=true). */ 7: optional bool isUnique,
	 /* The language used to express the derivation logic. */ 8: optional string derivationLanguage,
	 /* An executable expression of the derivation logic in the language above. */ 9: optional string derivationLogic,
}

struct File {
	 /*  */ 1: required string name,
	 /*  */ 2: required binary contents,
}

struct HawkInstance {
	 /* The name of the instance. */ 1: required string name,
	 /* Whether the instance is running or not. */ 2: required bool running,
}

struct IndexedAttributeSpec {
	 /* The URI of the metamodel to which the indexed attribute belongs. */ 1: required string metamodelUri,
	 /* The name of the type to which the indexed attribute belongs. */ 2: required string typeName,
	 /* The name of the indexed attribute. */ 3: required string attributeName,
}

struct ModelSpec {
	 /* The local name of the model in the transformation. */ 1: required string name,
	 /* The URI from which the model will be loaded or to which it will be persisted. */ 2: required string uri,
	 /* The URIs of the metamodels to which elements of the model conform. */ 3: required list<string> metamodelUris,
}

struct OperationModel {
}

struct Slot {
	 /* The name of the model element property the value of which is stored in this slot. */ 1: required string name,
	 /* The values of the slot. These can be primitive values (for attributes) or model elements (for references). */ 2: required list<string> values,
}

struct TransformationStatus {
	 /* True if the transformation has finished, false otherwise. */ 1: required bool finished,
	 /* Time passed since the start of execution. */ 2: required i64 elapsed,
	 /* Description of the error that caused the transformation to fail. */ 3: required string error,
}

struct UserProfile {
	 /* The real name of the user. */ 1: required string realName,
	 /* Whether the user has admin rights (i.e. so that they can create new users, change the status of admin users etc). */ 2: required bool admin,
}

struct CollaborationQueryInvocationSpecification {
	 /* Fully qualified name of the pre-existing query. */ 1: required string patternFQN,
	 /* Name/value bindings to be provided to the query. */ 2: required list<CollaborationQueryBinding> bindings,
}

struct CollaborationResource {
	 /* File with the contents of the resource. */ 1: required File file,
}

struct ModelElement {
	 /* Unique ID of the model element. */ 1: required string id,
	 /* URI of the metamodel to which the type of the element belongs. */ 2: required string metamodelUri,
	 /* The name of type that the model element is an instance of. */ 3: required string typeName,
	 /* Slots holding the values of the model element's attributes. */ 4: required list<Slot> attributes,
	 /* Slots holding the values of the model element's references. */ 5: required list<Slot> references,
}

struct ModelElementChange {
	 /* The model element that was changed. */ 1: required ModelElement element,
	 /* The type of change performed on the model. */ 2: required ModelElementChangeType type,
	 /* For changes of type UPDATED, the path within the element that was updated. */ 3: optional string changePath,
}


exception AuthenticationFailed {
}

exception CollaborationLockQueryNotFound {
}

exception CollaborationResourceLocked {
	 /* Reference to the locked resource. */ 1: required CollaborationResourceReference resourceReference,
}

exception CollaborationResourceNotFound {
	 /* Reference to the missing resource. */ 1: required CollaborationResourceReference resourceReference,
}

exception HawkInstanceNotFound {
}

exception InvalidCollaborationLockQuerySpec {
}

exception InvalidDerivedAttributeSpec {
	 /* Reason for the spec not being valid. */ 1: required string reason,
}

exception InvalidIndexedAttributeSpec {
	 /* Reason for the spec not being valid. */ 1: required string reason,
}

exception InvalidMetamodel {
	 /* Reason for the metamodel not being valid. */ 1: required string reason,
}

exception InvalidModelSpec {
	 /* A copy of the invalid model specification. */ 1: required ModelSpec spec,
	 /* Reason for the spec not being valid. */ 2: required string reason,
}

exception InvalidPollingConfiguration {
	 /* Reason for the spec not being valid. */ 1: required string reason,
}

exception InvalidQuery {
	 /* Reason for the query not being valid. */ 1: required string reason,
}

exception InvalidTransformation {
	 /* Reason for the transformation not being valid. */ 1: required string reason,
	 /* Location of the problem, if applicable. Usually a combination of line and column numbers. */ 2: required string location,
}

exception MergeRequired {
}

exception TransformationTokenNotFound {
	 /* Transformation token which was not found within the invoked MONDO instance. */ 1: required string token,
}

exception UnknownQueryLanguage {
}

exception UnknownRepositoryType {
}

exception UserExists {
}

exception UserNotFound {
}

exception VCSAuthenticationFailed {
}

exception VCSAuthorizationFailed {
}

/* The majority of service operations provided by the MONDO
   		platform require user authentication (indicated in the top-left  
   		cell of each operation table) to prevent unaccountable use. 
   		As such, the platform needs to provide basic user management service operations 
   		for creating, updating and deleting user accounts. */
service Users {
  /* Creates a new platform user. Auth needed: Yes */
  void createUser(
	/* A unique identifier for the user. */ 1: required string username, 
	/* The desired password. */ 2: required string password, 
	/* The profile of the user. */ 3: required UserProfile profile, 
  )
  throws (
	1: UserExists err1 /* The specified username already exists. */ 
	) 
	
  /* Tests whether the provided credentials are valid. Auth needed: No */
  bool testCredentials(
	/* A user name. */ 1: required string username, 
	/* The password for that user name. */ 2: required string password, 
  )
	
  /* Updates the profile of a platform user. Auth needed: Yes */
  void updateUser(
	/* The name of the user to update the profile of. */ 1: required string username, 
	/* The updated profile of the user. */ 2: required UserProfile profile, 
  )
  throws (
	1: UserNotFound err1 /* The specified username does not exist. */ 
	) 
	
  /* Deletes a platform user. Auth needed: Yes */
  void deleteUser(
	/* The name of the user to delete. */ 1: required string username, 
  )
  throws (
	1: UserNotFound err1 /* The specified username does not exist. */ 
	) 
	
}

/* The following service operations expose the capabilities of the Hawk heterogeneous model indexing
   framework developed in Work Package 5. The framework is discussed in detail in D5.2 and D5.3. */
service Hawk {
  /* Creates a new Hawk instance (stopped). Auth needed: Yes */
  void createInstance(
	/* The unique name of the new Hawk instance. */ 1: required string name, 
  )
	
  /* Lists the details of all Hawk instances. Auth needed: Yes */
  list<HawkInstance> listInstances(
  )
	
  /* Removes an existing Hawk instance. Auth needed: Yes */
  void removeInstance(
	/* The name of the Hawk instance to remove. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Starts a stopped Hawk instance. Auth needed: Yes */
  void startInstance(
	/* The name of the Hawk instance to start. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Stops a running Hawk instance. Auth needed: Yes */
  void stopInstance(
	/* The name of the Hawk instance to stop. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Registers a set of file-based metamodels with a Hawk instance. Auth needed: Yes */
  void registerMetamodels(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The metamodels to register. 
	   			More than one metamodel files can be provided in one 
	   			go to accomodate fragmented metamodels. */ 2: required list<File> metamodel, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: InvalidMetamodel err2 /* The provided metamodel is not valid (e.g. unparsable or inconsistent). */ 
	) 
	
  /* Unregisters a metamodel from a Hawk instance. Auth needed: Yes */
  void unregisterMetamodel(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the metamodel. */ 2: required string metamodel, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the URIs of the registered metamodels of a Hawk instance. Auth needed: Yes */
  list<string> listMetamodels(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the supported query languages and their status. Auth needed: Yes */
  list<string> listQueryLanguages(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
	
  /* Runs a query on a Hawk instance and returns a collection of primitives and/or model elements (see ModelElement struct). Auth needed: Yes */
  list<string> query(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The query to be executed. */ 2: required string query, 
	/* The name of the query language used (e.g. EOL, OCL). */ 3: required string language, 
	/* The scope of the query (e.g. *.uml). */ 4: required string scope, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: UnknownQueryLanguage err2 /* The specified query language is not supported by the operation. */ 
	3: InvalidQuery err3 /* The specified query is not valid. */ 
	) 
	
  /* Returns populated model elements for the provided proxies. Auth needed: Yes */
  list<ModelElement> resolveProxies(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* Proxy model element IDs to be resolved. */ 2: required list<string> ids, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Asks a Hawk instance to start monitoring a repository. Auth needed: Yes */
  void addRepository(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the repository to monitor. */ 2: required string uri, 
	/* The type of repository to be monitored. */ 3: required string type, 
	/* A valid set of credentials that has read-access to the repository. */ 4: optional Credentials credentials, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: UnknownRepositoryType err2 /* The specified repository type is not supported by the operation. */ 
	3: VCSAuthenticationFailed err3 /* The client failed to prove its identity in the VCS. */ 
	) 
	
  /* Asks a Hawk instance to stop monitoring a repository. Auth needed: Yes */
  void removeRepository(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the repository to stop monitoring. */ 2: required string uri, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the URIs of the repositories monitored by a Hawk instance. Auth needed: Yes */
  list<string> listRepositories(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the available repository types in this installation. Auth needed: Yes */
  list<string> listRepositoryTypes(
  )
	
  /* Lists the paths of the files of the indexed repository. Auth needed: Yes */
  list<string> listFiles(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the indexed repository. */ 2: required string repository, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Sets the base polling period and max interval of a Hawk instance. Auth needed: Yes */
  void configurePolling(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The base polling period (in seconds). */ 2: required i32 base, 
	/* The maximum polling interval (in seconds). */ 3: required i32 max, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: InvalidPollingConfiguration err2 /* The polling configuration is not valid. */ 
	) 
	
  /* Add a new derived attribute to a Hawk instance. Auth needed: Yes */
  void addDerivedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the new derived attribute. */ 2: required DerivedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: InvalidDerivedAttributeSpec err2 /* The derived attribute specification is not valid. */ 
	) 
	
  /* Remove a derived attribute from a Hawk instance. Auth needed: Yes */
  void removeDerivedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the derived attribute to be removed.
	   			Only the first three fields of the spec
	   			need to be populated. */ 2: required DerivedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the derived attributes of a Hawk instance. Only the first three fields of the spec are currently populated. Auth needed: Yes */
  list<DerivedAttributeSpec> listDerivedAttributes(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Add a new indexed attribute to a Hawk instance. Auth needed: Yes */
  void addIndexedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the new indexed attribute. */ 2: required IndexedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: InvalidIndexedAttributeSpec err2 /* The indexed attribute specification is not valid. */ 
	) 
	
  /* Remove a indexed attribute from a Hawk instance. Auth needed: Yes */
  void removeIndexedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the indexed attribute to be removed. */ 2: required IndexedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Lists the indexed attributes of a Hawk instance. Auth needed: Yes */
  list<IndexedAttributeSpec> listIndexedAttributes(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Returns the full contents of a Hawk instance. Cross-model references are also resolved. Auth needed: Yes */
  list<ModelElement> getAllContents(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
  /* Returns the contents of one or more models indexed in a Hawk instance. Cross-model references are also resolved. Auth needed: Yes */
  list<ModelElement> getModel(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the repository in which the model is contained. */ 2: required string repositoryUri, 
	/* The path of the model file(s) in the repository. */ 3: required string filePath, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	) 
	
}

/* The following service operations expose the capabilities of the offline collaboration framework 
   developed in Work Package 4. The framework is discussed in detail in D4.3. */
service OfflineCollaboration {
  /* Performs the checkout operation. Auth needed: Yes */
  list<CollaborationResource> checkout(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The references to the required resources. */ 2: required list<CollaborationResourceReference> resources, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: CollaborationResourceNotFound err3 /* The resource does not exist in the VCS. */ 
	) 
	
  /* Performs the commit operation. Auth needed: Yes */
  void commit(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The references to the required resources. */ 2: required list<CollaborationResourceReference> resources, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: CollaborationResourceNotFound err3 /* The resource does not exist in the VCS. */ 
	4: CollaborationResourceLocked err4 /* The resource is currently locked for collaboration. */ 
	) 
	
  /* Performs the pull operation. Auth needed: Yes */
  list<CollaborationResource> pull(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The references to the required resources. */ 2: required list<CollaborationResourceReference> resources, 
	/* The operations executed on the client. */ 3: required OperationModel operationModel, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: CollaborationResourceNotFound err3 /* The resource does not exist in the VCS. */ 
	4: MergeRequired err4 /* The operation requires a merge before it can be retried. */ 
	) 
	
  /* Publishes a lock definition. Auth needed: Yes */
  void publishLockDefinition(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The lock query specification. */ 2: required CollaborationLockQuerySpec specification, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: InvalidCollaborationLockQuerySpec err3 /* The lock query specification is not valid. */ 
	) 
	
  /* Unpublish a lock definition. Auth needed: Yes */
  void unpublishLockDefinition(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The lock query specification. */ 2: required CollaborationLockQuerySpec specification, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: InvalidCollaborationLockQuerySpec err3 /* The lock query specification is not valid. */ 
	4: CollaborationLockQueryNotFound err4 /* No matching lock exists. */ 
	) 
	
  /* Locks the pattern with the given bindings. Auth needed: Yes */
  void lock(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The lock specification with pattern and its bindings. */ 2: required CollaborationQueryInvocationSpecification specification, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: InvalidCollaborationLockQuerySpec err3 /* The lock query specification is not valid. */ 
	4: CollaborationResourceLocked err4 /* The resource is currently locked for collaboration. */ 
	) 
	
  /* Unlocks the pattern with the given bindings. Auth needed: Yes */
  void unlock(
	/* The credentials of the user in the underlying VCS. */ 1: required Credentials credentials, 
	/* The lock specification with pattern and its bindings. */ 2: required CollaborationQueryInvocationSpecification specification, 
  )
  throws (
	1: VCSAuthenticationFailed err1 /* The client failed to prove its identity in the VCS. */ 
	2: VCSAuthorizationFailed err2 /* The client does not have the required permissions in the VCS to perform the operation. */ 
	3: InvalidCollaborationLockQuerySpec err3 /* The lock query specification is not valid. */ 
	4: CollaborationLockQueryNotFound err4 /* No matching lock exists. */ 
	) 
	
}

/* The following service operations expose the capabilities of the cloud-enabled
   version of the ATL transformation language which is currently under development and
   will be presented in M24 in D3.3. */
service CloudATL {
  /* Invokes a cloud-based transformation in a batch non-blocking mode. 
     			Returns a token that can be used to check the status of the transformation. Auth needed: Yes */
  string launch(
	/* The ATL source-code of the transformation. */ 1: required string transformation, 
	/* The input models of the transformation. */ 2: required list<ModelSpec> source, 
	/* The target models of the transformation. */ 3: required list<ModelSpec> target, 
  )
  throws (
	1: InvalidTransformation err1 /* The transformation is not valid: it is unparsable or inconsistent. */ 
	2: InvalidModelSpec err2 /* The model specification is not valid: the model or the metamodels are inaccessible or invalid. */ 
	) 
	
  /* Returns the status of a previously invoked transformation. Auth needed: Yes */
  TransformationStatus getStatus(
	/* A valid token returned by a previous call to launch(). */ 1: required string token, 
  )
  throws (
	1: TransformationTokenNotFound err1 /* The specified transformation token does not exist within the invokved MONDO instance. */ 
	) 
	
}

/* The following service operations expose the capabilities of the reactive
   version of the ATL transformation language which is discussed in D3.2. */
service ReactiveATL {
  /* Launches a cloud-based transformation in reactive mode.
     	    The transformation keeps running until it is explicitly stopped.
     	    Returns a token that can be used to control the transformation. Auth needed: Yes */
  string launch(
	/* The ATL source-code of the transformation. */ 1: required string transformation, 
	/* The input models of the transformation. */ 2: required list<ModelSpec> source, 
	/* The target models of the transformation. */ 3: required list<ModelSpec> target, 
  )
  throws (
	1: InvalidTransformation err1 /* The transformation is not valid: it is unparsable or inconsistent. */ 
	2: InvalidModelSpec err2 /* The model specification is not valid: the model or the metamodels are inaccessible or invalid. */ 
	) 
	
  /* Stops a cloud-based reactive transformation. Auth needed: Yes */
  string stop(
	/* A valid token returned by a previous call to launch(). */ 1: required string token, 
  )
  throws (
	1: TransformationTokenNotFound err1 /* The specified transformation token does not exist within the invokved MONDO instance. */ 
	) 
	
  /* Commits in-memory changes on the target model to its persistent storage. Auth needed: Yes */
  string commit(
	/* A valid token returned by a previous call to launch(). */ 1: required string token, 
  )
  throws (
	1: TransformationTokenNotFound err1 /* The specified transformation token does not exist within the invokved MONDO instance. */ 
	) 
	
}

