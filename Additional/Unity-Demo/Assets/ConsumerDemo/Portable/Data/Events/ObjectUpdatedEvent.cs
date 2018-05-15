namespace BAALLClient.Portable.Data
{
    public class ObjectUpdatedEvent
    {
        public ObjectUpdatedEvent(){
            Identifier = "";
        }

        public string Identifier{
            get;
            set;
        }
    }
}