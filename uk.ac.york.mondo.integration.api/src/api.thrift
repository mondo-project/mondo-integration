namespace java uk.ac.york.mondo.integration.api

enum CommitItemChangeType {
		/*  */ ADDED 
		/*  */ DELETED 
		/*  */ REPLACED 
		/*  */ UNKNOWN 
		/*  */ UPDATED 
}

enum SubscriptionDurability {
		/* Subscription survives client disconnections but not server restarts. */ DEFAULT 
		/* Subscription survives client disconnections and server restarts. */ DURABLE 
		/* Subscription removed after disconnecting. */ TEMPORARY 
}


exception AuthenticationFailed {
}

struct CollaborationGitResourceReference {
	 /* The URI of the repository containing the resource. */ 1: required string repositoryUri,
	 /* The name of the Git branch to which new commits should be pushed. */ 2: required string branch,
	 /* The SHA1 identifier of the commit with the resource. */ 3: required string commit,
}

exception CollaborationLockQueryNotFound {
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

struct CommitItem {
	 /*  */ 1: required string repoURL,
	 /*  */ 2: required string revision,
	 /*  */ 3: required string path,
	 /*  */ 4: required CommitItemChangeType type,
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

exception HawkInstanceNotFound {
}

exception HawkInstanceNotRunning {
}

struct IndexedAttributeSpec {
	 /* The URI of the metamodel to which the indexed attribute belongs. */ 1: required string metamodelUri,
	 /* The name of the type to which the indexed attribute belongs. */ 2: required string typeName,
	 /* The name of the indexed attribute. */ 3: required string attributeName,
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

union MixedReference {
	 /* Identifier-based reference to a model element. */ 1: optional string id,
	 /* Position-based reference to a model element. */ 2: optional i32 position,
}

struct ModelSpec {
	 /* The URI from which the model will be loaded or to which it will be persisted. */ 1: required string uri,
	 /* The URIs of the metamodels to which elements of the model conform. */ 2: required list<string> metamodelUris,
}

struct OperationModel {
}

struct Repository {
	 /* The URI to the repository. */ 1: required string uri,
	 /* The type of repository. */ 2: required string type,
}

union ScalarOrReference {
	 /*  */ 1: optional bool vBoolean,
	 /*  */ 2: optional byte vByte,
	 /*  */ 3: optional i16 vShort,
	 /*  */ 4: optional i32 vInteger,
	 /*  */ 5: optional i64 vLong,
	 /*  */ 6: optional i64 vReference,
	 /*  */ 7: optional double vDouble,
	 /*  */ 8: optional string vString,
}

struct Slot {
	 /* The name of the model element property the value of which is stored in this slot. */ 1: required string name,
}

struct Subscription {
	 /* Host name of the message queue server. */ 1: required string host,
	 /* Port in which the message queue server is listening. */ 2: required i32 port,
	 /* Address of the topic queue. */ 3: required string queueAddress,
	 /* Name of the topic queue. */ 4: required string queueName,
}

struct TransformationStatus {
	 /* True if the transformation has finished, false otherwise. */ 1: required bool finished,
	 /* Time passed since the start of execution. */ 2: required i64 elapsed,
	 /* Description of the error that caused the transformation to fail. */ 3: required string error,
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

struct UserProfile {
	 /* The real name of the user. */ 1: required string realName,
	 /* Whether the user has admin rights (i.e. so that they can create new users, change the status of admin users etc). */ 2: required bool admin,
}

exception VCSAuthenticationFailed {
}

exception VCSAuthorizationFailed {
}

union Variant {
	 /*  */ 1: optional byte vByte,
	 /*  */ 2: optional bool vBoolean,
	 /*  */ 3: optional i16 vShort,
	 /*  */ 4: optional i32 vInteger,
	 /*  */ 5: optional i64 vLong,
	 /*  */ 6: optional double vDouble,
	 /*  */ 7: optional string vString,
	 /*  */ 8: optional binary vBytes,
	 /*  */ 9: optional list<bool> vBooleans,
	 /*  */ 10: optional list<i16> vShorts,
	 /*  */ 11: optional list<i32> vIntegers,
	 /*  */ 12: optional list<i64> vLongs,
	 /*  */ 13: optional list<double> vDoubles,
	 /*  */ 14: optional list<string> vStrings,
}

struct AttributeSlot {
	 /* The name of the model element property the value of which is stored in this slot. */ 1: required string name,
	 /* Variant value of the slot. */ 2: required Variant value,
}

struct CollaborationQueryInvocationSpecification {
	 /* Fully qualified name of the pre-existing query. */ 1: required string patternFQN,
	 /* Name/value bindings to be provided to the query. */ 2: required list<CollaborationQueryBinding> bindings,
}

struct CollaborationResource {
	 /* File with the contents of the resource. */ 1: required File file,
}

exception CollaborationResourceLocked {
	 /* Reference to the locked resource. */ 1: required CollaborationResourceReference resourceReference,
}

exception CollaborationResourceNotFound {
	 /* Reference to the missing resource. */ 1: required CollaborationResourceReference resourceReference,
}

struct HawkAttributeRemovalEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Identifier of the model element that was changed. */ 2: required string id,
	 /* Name of the attribute that was removed. */ 3: required string attribute,
}

struct HawkAttributeUpdateEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Identifier of the model element that was changed. */ 2: required string id,
	 /* Name of the attribute that was changed. */ 3: required string attribute,
	 /* New value for the attribute. */ 4: required Variant value,
}

struct HawkModelElementAdditionEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Metamodel URI of the type of the model element. */ 2: required string metamodelURI,
	 /* Name of the type of the model element. */ 3: required string typeName,
	 /* Identifier of the model element that was added. */ 4: required string id,
}

struct HawkModelElementRemovalEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Identifier of the model element that was removed. */ 2: required string id,
}

struct HawkReferenceAdditionEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Identifier of the source model element. */ 2: required string sourceId,
	 /* Identifier of the target model element. */ 3: required string targetId,
	 /* Name of the reference that was added. */ 4: required string refName,
}

struct HawkReferenceRemovalEvent {
	 /* Entry within the commit that produced the changes. */ 1: required CommitItem vcsItem,
	 /* Identifier of the source model element. */ 2: required string sourceId,
	 /* Identifier of the target model element. */ 3: required string targetId,
	 /* Name of the reference that was removed. */ 4: required string refName,
}

exception InvalidModelSpec {
	 /* A copy of the invalid model specification. */ 1: required ModelSpec spec,
	 /* Reason for the spec not being valid. */ 2: required string reason,
}

struct ReferenceSlot {
	 /* The name of the model element property the value of which is stored in this slot. */ 1: required string name,
	 /* Position of the referenced element (if there is only one position-based reference in this slot). */ 2: optional i32 position,
	 /* Positions of the referenced elements (if more than one). */ 3: optional list<i32> positions,
	 /* Unique identifier of the referenced element (if there is only one ID based reference in this slot). */ 4: optional string id,
	 /* Unique identifiers of the referenced elements (if more than one). */ 5: optional list<string> ids,
	 /* Mix of identifier- and position-bsaed references (if there is at least one position and one ID. */ 6: optional list<MixedReference> mixed,
}

union HawkChangeEvent {
	 /* A model element was added. */ 1: optional HawkModelElementAdditionEvent modelElementAddition,
	 /* A model element was removed. */ 2: optional HawkModelElementRemovalEvent modelElementRemoval,
	 /* An attribute was updated. */ 3: optional HawkAttributeUpdateEvent modelElementAttributeUpdate,
	 /* An attribute was removed. */ 4: optional HawkAttributeRemovalEvent modelElementAttributeRemoval,
	 /* A reference was added. */ 5: optional HawkReferenceAdditionEvent referenceAddition,
	 /* A reference was removed. */ 6: optional HawkReferenceRemovalEvent referenceRemoval,
}

struct ModelElement {
	 /* Unique ID of the model element (not set if using position-based references). */ 1: optional string id,
	 /* URI of the metamodel to which the type of the element belongs (not set if equal to that of the previous model element). */ 2: optional string metamodelUri,
	 /* Name of the type that the model element is an instance of (not set if equal to that of the previous model element). */ 3: optional string typeName,
	 /* Slots holding the values of the model element's attributes, if any have been set. */ 4: optional list<AttributeSlot> attributes,
	 /* Slots holding the values of the model element's references, if any have been set. */ 5: optional list<ReferenceSlot> references,
	 /* Slots holding contained model elements, if any have been set. */ 6: optional list<ContainerSlot> containers,
}

struct ContainerSlot {
	 /* The name of the model element property the value of which is stored in this slot. */ 1: required string name,
	 /* Contained elements for this slot. */ 2: required list<ModelElement> elements,
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
	/* The admin password for encrypting credentials. */ 2: required string adminPassword, 
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
	/* The admin password for encrypting credentials. */ 2: required string adminPassword, 
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
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
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
	3: HawkInstanceNotRunning err3 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Unregisters a metamodel from a Hawk instance. Auth needed: Yes */
  void unregisterMetamodel(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the metamodel. */ 2: required string metamodel, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the URIs of the registered metamodels of a Hawk instance. Auth needed: Yes */
  list<string> listMetamodels(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the supported query languages and their status. Auth needed: Yes */
  list<string> listQueryLanguages(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
	
  /* Runs a query on a Hawk instance and returns a collection of primitives and/or model elements (see ModelElement struct). Auth needed: Yes */
  list<ScalarOrReference> query(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The query to be executed. */ 2: required string query, 
	/* The name of the query language used (e.g. EOL, OCL). */ 3: required string language, 
	/* The repository for the query (or * for all repositories). */ 4: required string repository,
	/* The scope of the query (e.g. *.uml). */ 5: required string scope,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	3: UnknownQueryLanguage err3 /* The specified query language is not supported by the operation. */ 
	4: InvalidQuery err4 /* The specified query is not valid. */ 
	) 
	
  /* Returns populated model elements for the provided proxies. Auth needed: Yes */
  list<ModelElement> resolveProxies(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* Proxy model element IDs to be resolved. */ 2: required list<string> ids, 
	/* Whether to include attributes (true) or not (false). */ 3:  bool includeAttributes = true,
	/* Whether to include references (true) or not (false). */ 4:  bool includeReferences = true,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Asks a Hawk instance to start monitoring a repository. Auth needed: Yes */
  void addRepository(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The repository to monitor. */ 2: required Repository repo,
	/* A valid set of credentials that has read-access to the repository. */ 3:  Credentials credentials,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	3: UnknownRepositoryType err3 /* The specified repository type is not supported by the operation. */ 
	4: VCSAuthenticationFailed err4 /* The client failed to prove its identity in the VCS. */ 
	) 
	
  /* Asks a Hawk instance to stop monitoring a repository. Auth needed: Yes */
  void removeRepository(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the repository to stop monitoring. */ 2: required string uri, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Changes the credentials used to monitor a repository. Auth needed: Yes */
  void updateRepositoryCredentials(
	/* The name of the Hawk instance. */ 1: required string name,
	/* The URI of the repository to update. */ 2: required string uri,
	/* The new credentials to be used. */ 3: required Credentials cred,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the repositories monitored by a Hawk instance. Auth needed: Yes */
  list<Repository> listRepositories(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the available repository types in this installation. Auth needed: Yes */
  list<string> listRepositoryTypes(
  )
	
  /* Lists the paths of the files of the indexed repository. Auth needed: Yes */
  list<string> listFiles(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the indexed repository. */ 2: required list<string> repository,
	/* File name patterns to search for (* lists all files). */ 3: required list<string> filePatterns,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Sets the base polling period and max interval of a Hawk instance. Auth needed: Yes */
  void configurePolling(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The base polling period (in seconds). */ 2: required i32 base, 
	/* The maximum polling interval (in seconds). */ 3: required i32 max, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	3: InvalidPollingConfiguration err3 /* The polling configuration is not valid. */ 
	) 
	
  /* Add a new derived attribute to a Hawk instance. Auth needed: Yes */
  void addDerivedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the new derived attribute. */ 2: required DerivedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	3: InvalidDerivedAttributeSpec err3 /* The derived attribute specification is not valid. */ 
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
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the derived attributes of a Hawk instance. Only the first three fields of the spec are currently populated. Auth needed: Yes */
  list<DerivedAttributeSpec> listDerivedAttributes(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Add a new indexed attribute to a Hawk instance. Auth needed: Yes */
  void addIndexedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the new indexed attribute. */ 2: required IndexedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	3: InvalidIndexedAttributeSpec err3 /* The indexed attribute specification is not valid. */ 
	) 
	
  /* Remove a indexed attribute from a Hawk instance. Auth needed: Yes */
  void removeIndexedAttribute(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The details of the indexed attribute to be removed. */ 2: required IndexedAttributeSpec spec, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Lists the indexed attributes of a Hawk instance. Auth needed: Yes */
  list<IndexedAttributeSpec> listIndexedAttributes(
	/* The name of the Hawk instance. */ 1: required string name, 
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Returns the contents of one or more models indexed in a Hawk instance. Cross-model references are also resolved. Auth needed: Yes */
  list<ModelElement> getModel(
	/* The name of the Hawk instance. */ 1: required string name, 
	/* The URI of the repository in which the model is contained. */ 2: required list<string> repositoryUri,
	/* The pattern(s) for the model file(s) in the repository. */ 3: required list<string> filePath, 
	/* Whether to include attributes (true) or not (false). */ 4:  bool includeAttributes = true,
	/* Whether to include references (true) or not (false). */ 5:  bool includeReferences = true,
	/* Whether to include node IDs (true) or not (false). */ 6:  bool includeNodeIDs = false,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
	) 
	
  /* Returns the root objects of one or more models indexed in a Hawk instance. Auth needed: Yes */
  list<ModelElement> getRootElements(
	/* The name of the Hawk instance. */ 1: required string name,
	/* The URI of the repository in which the model is contained. */ 2: required list<string> repositoryUri,
	/* The pattern(s) for the model file(s) in the repository. */ 3: required list<string> filePath,
	/* Whether to include attributes (true) or not (false). */ 4:  bool includeAttributes = true,
	/* Whether to include references (true) or not (false). */ 5:  bool includeReferences = true,
  )
	
  /* Returns subscription details to a queue of HawkChangeEvents with notifications about changes to a set of indexed models. Auth needed: Yes */
  Subscription watchModelChanges(
	/* The name of the Hawk instance. */ 1: required string name,
	/* The URI of the repository in which the model is contained. */ 2: required string repositoryUri,
	/* The pattern(s) for the model file(s) in the repository. */ 3: required list<string> filePath,
	/* Unique client ID (used as suffix for the queue name). */ 4: required string clientID,
	/* Durability of the subscription. */ 5: required SubscriptionDurability durableEvents,
  )
  throws (
	1: HawkInstanceNotFound err1 /* No Hawk instance exists with that name. */ 
	2: HawkInstanceNotRunning err2 /* The selected Hawk instance is not running. */ 
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
	/* The input models of the transformation. */ 2: required ModelSpec source, 
	/* The target models of the transformation. */ 3: required ModelSpec target, 
  )
  throws (
	1: InvalidTransformation err1 /* The transformation is not valid: it is unparsable or inconsistent. */ 
	2: InvalidModelSpec err2 /* The model specification is not valid: the model or the metamodels are inaccessible or invalid. */ 
	) 
	
  /* Lists the ids of the transformation jobs tracked by this server. Auth needed: Yes */
  list<string> getJobs(
  )
	
  /* Returns the status of a previously invoked transformation. Auth needed: Yes */
  TransformationStatus getStatus(
	/* A valid token returned by a previous call to launch(). */ 1: required string token, 
  )
  throws (
	1: TransformationTokenNotFound err1 /* The specified transformation token does not exist within the invokved MONDO instance. */ 
	) 
	
  /* Kills a previously invoked transformation. Auth needed: Yes */
  void kill(
	/* A valid token returned by a previous call to launch(). */ 1: required string token, 
  )
  throws (
	1: TransformationTokenNotFound err1 /* The specified transformation token does not exist within the invokved MONDO instance. */ 
	) 
	
}

